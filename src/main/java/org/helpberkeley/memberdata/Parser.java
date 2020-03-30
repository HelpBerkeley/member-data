/******************************************************************************
 * Copyright (c) 2020 helpberkeley.org
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/

package org.helpberkeley.memberdata;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {

    static List<Long> activeUserIds(final String activeUsers) {

        List<Long> activeUserIds = new ArrayList<>();
        Map<String, Object> options = new HashMap<>();
        options.put(JsonReader.USE_MAPS, Boolean.TRUE);

        Object obj = JsonReader.jsonToJava(activeUsers, options);
        Object[] users = (Object[])obj;

        for (Object user : users) {
            // FIX THIS, DS: learn how to do this with this cast
            Map<String, Object> u = (Map<String, Object>) user;

            Long id = (Long)u.get("id");
            activeUserIds.add(id);
        }

        return activeUserIds;
    }

    static User user(final String userJson) throws UserException {
        Map<String, Object> options = new HashMap<>();
        options.put(JsonReader.USE_MAPS, Boolean.TRUE);

        Object obj = JsonReader.jsonToJava(userJson, options);
        // FIX THIS, DS: learn how to do this with this cast
        return User.createUser((Map<String, Object>)obj);
    }

    static Group group(final String userJson) {
        Map<String, Object> options = new HashMap<>();
        options.put(JsonReader.USE_MAPS, Boolean.TRUE);

        Object obj = JsonReader.jsonToJava(userJson, options);
        Object groupObj = ((Map<String, Object>)obj).get("group");

        // FIX THIS, DS: learn how to do this with this cast
        return Group.createGroup((Map<String, Object>)groupObj);
    }

    static List<String> groupNames(final String groupsJson) {

        List<String> groupNames = new ArrayList<>();
        Map<String, Object> options = new HashMap<>();
        options.put(JsonReader.USE_MAPS, Boolean.TRUE);

        Object obj = JsonReader.jsonToJava(groupsJson, options);
        Object groupsObj = ((Map<String, Object>)obj).get("groups");

        for (Object groupObj : (Object[])groupsObj) {

            Map<String, Object> g = (Map<String, Object>) groupObj;
            groupNames.add((String)g.get("name"));
        }

        return groupNames;
    }
    static List<String> groupMembers(final String groupMembersJson) {

        List<String> groupMembers = new ArrayList<>();
        Map<String, Object> options = new HashMap<>();
        options.put(JsonReader.USE_MAPS, Boolean.TRUE);


        Object obj = JsonReader.jsonToJava(groupMembersJson, options);
        Object membersObj = ((Map<String, Object>)obj).get("members");

        for (Object memberObj : (Object[])membersObj) {

            Map<String, Object> u = (Map<String, Object>) memberObj;
            groupMembers.add((String)u.get("username"));
        }

        return groupMembers;
    }

    static void prettyPrint(final String pageJson) {

        System.out.println(JsonWriter.formatJson(pageJson));

    }

    static List<Group> groups(final String groupsJson) {
        List<Group> groups = new ArrayList<>();
        Map<String, Object> options = new HashMap<>();
        options.put(JsonReader.USE_MAPS, Boolean.TRUE);

        Object obj = JsonReader.jsonToJava(groupsJson, options);
        Object groupsObj = ((Map<String, Object>)obj).get("groups");

        for (Object groupObj : (Object[])groupsObj) {
            groups.add(Group.createGroup((JsonObject)groupObj));
        }

        return groups;
    }
}
