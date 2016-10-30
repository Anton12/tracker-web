package edu.xored.tracker;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/issues")
public class IssueController {
    // TODO: temporary issues storage for testing purposes. Remove when implement access to real issues through CLI core.
    private static Map<Long, Issue> issueMap = new HashMap<Long, Issue>();

    static {
        Issue firstIssue = new Issue(
                0L,
                "First issue",
                "First issue's description",
                Issue.Status.OPEN
        );
        firstIssue.addComment(new Comment("some author", "some content"));

        Issue secondIssue = new Issue(
                1L,
                "Second issue",
                "Second issue's description",
                Issue.Status.CLOSED
        );
        secondIssue.addComment(new Comment("another author", "another content"));

        Issue thirdIssue = new Issue(
                2L,
                "Third issue",
                "Third issue's description",
                Issue.Status.OPEN
        );

        Issue fourthIssue = new Issue(
                3L,
                "Fourth issue",
                "Fourth issue's description",
                Issue.Status.CLOSED
        );

        issueMap.put(firstIssue.getHash(), firstIssue);
        issueMap.put(secondIssue.getHash(), secondIssue);
        issueMap.put(thirdIssue.getHash(), thirdIssue);
        issueMap.put(fourthIssue.getHash(), fourthIssue);
    }

    @GetMapping(value = "/{hash}")
    public Issue getIssue(@PathVariable("hash") long hash) {
        Issue issue = issueMap.get(hash);
        if (issue == null) {
            throw new IssueNotFoundException();
        }
        return issue;
    }

    @GetMapping
    public Collection<Issue> getIssues(@RequestParam(value = "status", required = false) Issue.Status status) {
        if (status == null) {
            return issueMap.values();
        }
        return issueMap.values()
                .stream()
                .filter(entry -> entry.getStatus() == status)
                .collect(Collectors.toList());
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Issue not found")
    private class IssueNotFoundException extends RuntimeException {
    }

    @PostMapping(value = "{issueHash}/comments")
    public void postComment(@RequestBody Comment comment, @PathVariable("issueHash") long hash) {
        Issue issue = issueMap.get(hash);
        issue.addComment(comment);
    }
}
