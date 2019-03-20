package com.example;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PatchTest {

    private static final String patchDirectory = "/Users/jackieoh/Desktop/PURE/patches/";

    @Test
    public void testStatusNoCleaners() {
        Patch patch = new Patch(patchDirectory + "com.alibaba.json.bvt.bug.Issue_717.test_for_issue.patch");
        assertEquals(Patch.Status.NO_CLEANERS, patch.getStatus());
    }

    @Test
    public void testStatusInlineFail() {
        Patch patch = new Patch(patchDirectory + "com.alibaba.json.bvt.date.DateTest_tz.test_codec.patch");
        assertEquals(Patch.Status.INLINE_FAIL, patch.getStatus());
    }

    @Test
    public void testStatusInlineSuccess() {
        Patch patch = new Patch(patchDirectory + "com.github.kevinsawicki.http.HttpRequestTest.deleteWithEscapedMappedQueryParams.patch");
        assertEquals(Patch.Status.INLINE_SUCCESS, patch.getStatus());
        System.out.println(patch.applyPatch());
    }
}
