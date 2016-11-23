package edu.xored.tracker;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(value = "/api/issues")
public class IssueController {

    private IssueRepository issueRepository = new IssueRepositoryImpl();

    @PostMapping
    public Issue postIssue(@RequestBody Issue issue) {
        return issueRepository.save(issue);
    }

    @GetMapping(value = "/{hash}")
    public Issue getIssue(@PathVariable("hash") String hash) {
        //assertIssueExists(hash);

        return issueRepository.findOne(hash);
    }

    @PutMapping(value = "/{hash}")
    public Issue putIssue(@PathVariable("hash") String hash,
                          @RequestBody Issue issue) {
        //assertIssueExists(hash);

        return issueRepository.replace(hash, issue);
    }

    @DeleteMapping(value = "/{hash}")
    public void deleteIssue(@PathVariable("hash") String hash) {
        assertIssueExists(hash);

        issueRepository.delete(hash);
    }

    @PatchMapping(value = "/{hash}")
    public Issue patchIssue(@PathVariable("hash") String hash,
                            @RequestBody Issue patchedIssue) {
        assertIssueExists(hash);

        return issueRepository.replace(hash, issueRepository.findOne(hash).updateIssue(patchedIssue));
    }

    @GetMapping
    public Collection<Issue> getIssues(@RequestParam(value = "status", required = false) Issue.Status status) {
        System.out.println(status);
        return StreamSupport.stream(issueRepository.findAll(status).spliterator(), false)
                .collect(Collectors.toList());
    }

    @PostMapping(value = "/{hash}/comments")
    public void postComment(@RequestBody Comment comment,
                            @PathVariable("hash") String hash) {
        assertIssueExists(hash);

        issueRepository.postComment(comment, hash);
    }

    private void assertIssueExists(String hash) throws IssueNotFoundException {
        if (!issueRepository.exists(hash)) {
            throw new IssueNotFoundException();
        }
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Issue not found")
    private class IssueNotFoundException extends RuntimeException {
    }
}
