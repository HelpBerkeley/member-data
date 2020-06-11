package org.helpberkeley.memberdata;

import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class RestaurantTemplateTest {
    @Test
    public void noDataTest() {
        Throwable thrown = catchThrowable(() -> new RestaurantTemplateParser(""));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(RestaurantTemplateParser.ERROR_NO_DATA);
    }

    @Test
    public void csvParseError() {
        final String badCSV = "This is neither poetry nor CSV data.\n1,2,3,4\n";
        Throwable thrown = catchThrowable(() -> new RestaurantTemplateParser(badCSV).restaurants());
        assertThat(thrown).isInstanceOf(MemberDataException.class);

        System.out.println(thrown);
        System.out.println(thrown.getMessage());
        System.out.println(thrown.getCause());
        System.out.println(thrown.getCause().getMessage());
    }
}
