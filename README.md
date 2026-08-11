member-data
---
A program to fetch member-data from helpberkeley.org, and post information
about it to back to topics on the site.

Currently, the site postings are designed to assist controlBlockRestaurant order
data reconciliation, driver dispatcher, and administrative member
data tasks.

### Building

*mvn install* builds the standard jar file and additional an uber jar file
containing all of the required dependencies.

*mvn test* runs local tests which use simulated data and do not require access
to the website. They supply their own credentials, so no memberdata.properties
file is needed to build or test - only to run against the live site.

The uber jar contains org.helpberkeley.memberdata.Main.main as the entry
point and requires authentication to be configured.


### Authentication Requirements:

A Java properties file named memberdata.properties must be created
and added somewhere within the classpath.

The properties file must contain the following properties:
* Api-Username - a username from the helpberkeley.org site.
* Api-Key - a valid API key for that user.

API keys are generated in the Admin->API page.

Example:

```
Api-Username   aloyisus
Api-Key        123456789abcdef987654321
GMaps-Api-Key  xyzzy12345
```

memberdata.properties is gitignored, so that real credentials are never
committed. Do not add one to the repository.

Some commands need more than an ordinary API key:

* delete-topic-image-posts and delete-posts permanently destroy posts. They
  need an admin API user, and the can_permanently_delete site setting enabled
  in Admin->Settings. Without both, Discourse refuses the deletion with a 403.
  Run either without the "force" argument first - that is a dry run which
  reports exactly what would be destroyed, and deletes nothing.

### Bulk deletion

If the list on hand identifies posts by topic and post number rather than by
post id, resolve-post-ids converts it. It takes a CSV file with topic_id and
post_number columns, runs the Get Topic Images data explorer query once per
distinct topic, and writes a post_id,post_number,topic_id file that
delete-posts reads directly:

```
resolve-post-ids topic-post-list-file [output-file]
```

Without an output file name it writes alongside the input, with -post-ids.csv
in place of the extension. Only posts that still hold a Discourse-hosted image
resolve; the rest are written to <output-file>.unresolved.csv, which is itself
a valid input so a corrected subset can be re-run. Note that a post which has
already been soft deleted is invisible to that query and so will land there.

Pacing works as it does for delete-posts, set with
-Dmemberdata.resolveNapMillis=<milliseconds>.

delete-posts takes a CSV file with post_id and post_number columns, as
exported from a data explorer query, and permanently deletes those posts.
Rows for a topic's first post are skipped and reported - deleting one would
delete the entire topic - and duplicate post ids are collapsed.

Deletion is two passes over the whole batch: soft delete everything, then
force destroy everything. Discourse refuses to permanently delete a post
until a timer since its soft delete expires, so deleting a few posts at a
time means waiting out that timer over and over. Run as one batch, the first
pass takes longer than the timer and no wait is needed at all.

Progress is journalled to <post-list-file>.journal as it goes. Re-running
resumes from there, skipping what already completed. This matters because a
soft deleted post is invisible to the data explorer queries that identify
posts to delete - an interrupted run without the journal would strand those
posts on the site with no way to find them again. Delete the journal to
start over from scratch.

Pacing between requests starts at 1100ms and is set with
-Dmemberdata.deleteNapMillis=<milliseconds>. That default keeps a run just
under 60 requests per minute, which is the default of the site's
max_admin_api_reqs_per_minute setting - a fixed one minute window, where
exceeding the limit costs a wait until the window rolls.

The pacing then adapts: rate limiting or a server error doubles the wait, and
a sustained clean period eases it back down, never below the configured floor.
Each change is logged with a running count, and the settled pacing is reported
at the end of each pass and at the end of the run - including when the run
fails or is interrupted, so a killed run still tells you what value to start
the next one with.

This means the site's rate limit does not have to be known in advance -
starting too fast costs some backing off rather than a failed run. A retry
waits the interval Discourse asks for, from the Retry-After header or the
wait_seconds value in the response, rather than a fixed ten seconds.

A post that cannot be deleted is reported and skipped rather than ending the
run, and is left out of the destroy pass. Five failures in a row does end the
run, on the grounds that the site is unwell rather than the posts being odd.

### Listing images

list-category-images writes CSV to stdout, a header and then one row per
Discourse-hosted image in a category:

```
list-category-images category-name > images.csv
```

The columns are topic_id, topic_name, post_number, post_id and image_name.
Those include the two that delete-posts reads, so the output feeds it with no
conversion step - the duplicate rows a post with several uploads produces
collapse to one post, and any first post is skipped. An empty category still
gets the header, so the result is always readable as CSV.

It runs the same per-topic query as resolve-post-ids, so a category of
hundreds of topics is hundreds of requests, and it paces and adapts the same
way - starting at 1100ms, set with -Dmemberdata.listNapMillis=<milliseconds>.
Without that pacing the listing runs in bursts that collect a rate limit
refusal every time the site's request window rolls, and those queries are
joins across posts, upload references and uploads rather than cheap reads.

A topic whose query fails is reported and left out of the listing rather than
ending the run, since listing changes nothing. Five in a row does end it.

###[License](LICENSE)
