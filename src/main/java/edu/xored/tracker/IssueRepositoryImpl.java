package edu.xored.tracker;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class IssueRepositoryImpl implements IssueRepository {
    private static final String GIT_BUG = "git bug ";
    private static final String GIT_BUG_NEW = "git bug new ";
    private static final String GIT_BUG_RESOLVE = "git bug resolve ";

    private Map<String, Issue> issuesMap = new HashMap<>();

    public <S extends Issue> S save(S issue) {
        Process theProcess;
        String commands =  GIT_BUG_NEW + "-m "  + '\"' + issue.getSummary()
                + "\\n" + issue.getDescription() + '\"' + "\n";
        try {
            theProcess = Runtime.getRuntime().exec("/bin/bash");
        } catch(IOException e) {
            throw new ExecutionFailedException();
        }
        try (BufferedWriter outStream = new BufferedWriter(new OutputStreamWriter(theProcess.getOutputStream()))) {
            outStream.write(commands);
        } catch(IOException e) {
            throw new ExecutionFailedException();
        }
        String info;
        try (BufferedReader inStream = new BufferedReader(new InputStreamReader(theProcess.getInputStream()))) {
            info = inStream.readLine();
            issue.setHash(info);
        } catch(IOException e) {
            throw new ExecutionFailedException();
        }
        issuesMap.put(issue.getHash(), issue);
        return issue;
    }

    public <S extends Issue> Iterable<S> save(Iterable<S> issues) {
        if (issues == null) {
            return null;
        }
        return StreamSupport.stream(issues.spliterator(), false)
                .map(this::save)
                .collect(Collectors.toList());
    }

    public Issue findOne(String issueId) {
        Process theProcess;
        try {
            theProcess = Runtime.getRuntime().exec(GIT_BUG + issueId);
        } catch(IOException e) {
            throw new ExecutionFailedException();
        }
        String info;
        Issue issue = new Issue();
        issue.setHash(issueId);
        try (BufferedReader inStream = new BufferedReader(new InputStreamReader(theProcess.getInputStream()))) {
            inStream.readLine();
            inStream.readLine();
            info = inStream.readLine();
            if(info.substring(8,12).equals("open")) {
                issue.setStatus(Issue.Status.OPEN);
            } else {
                issue.setStatus(Issue.Status.CLOSED);
            }
            info = inStream.readLine();
            issue.setSummary(info.toString().substring(9));
            char[] descriptionData = new char[200];
            inStream.read(descriptionData,0,140);
            issue.setDescription(String.valueOf(descriptionData).substring(0,String.valueOf(descriptionData).indexOf("\u0000")));
            return issue;
        } catch(IOException e) {
            throw new ExecutionFailedException();
        }
    }

    public boolean exists(String issueId) {
        Process theProcess;
        try {
            theProcess = Runtime.getRuntime().exec(GIT_BUG + issueId);
        } catch(IOException e) {
            throw new ExecutionFailedException();
        }
        String info;
        try (BufferedReader inStream = new BufferedReader(new InputStreamReader(theProcess.getInputStream()))) {
            info = inStream.readLine();
            if(info.substring(0,5).equals("usage")) {
                return false;
            }
            return true;
        } catch(IOException e) {
            throw new ExecutionFailedException();
        }
    }

    public Iterable<Issue> findAll() {
        Issue.Status a = null;
        return findAll(a);
    }

    public Iterable<Issue> findAll(Iterable<String> issuesId) {
        List<Issue> result = new ArrayList<>();
        issuesId.forEach(issueId -> result.add(findOne(issueId)));
        return result;
    }

    public long count() {
        return (issuesMap == null)
                ? 0
                : issuesMap.size();
    }

    public void delete(String issueId) {
        throw new IssueNotImplementedException();
    }

    public void delete(Issue issue) {
        throw new IssueNotImplementedException();
    }

    public void delete(Iterable<? extends Issue> issuesId) {
        throw new IssueNotImplementedException();
    }

    public void deleteAll() {
        throw new IssueNotImplementedException();
    }

    public List<Issue> list() {
        return StreamSupport.stream(findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    public Issue replace(String hash, Issue issue) {
        Process theProcess;
        Issue old = issue;
        try {
            theProcess = Runtime.getRuntime().exec(GIT_BUG_RESOLVE + hash);
        } catch(IOException e) {
            throw new ExecutionFailedException();
        }
        String info;
        try (BufferedReader inStream = new BufferedReader(new InputStreamReader(theProcess.getInputStream()))) {
            info = inStream.readLine();
            if(info.substring(0,5).equals("Error")) {
                throw new IssueController.IssueNotFoundException();
            }
        } catch(IOException e) {
            throw new ExecutionFailedException();
        }
        issue.setStatus(Issue.Status.CLOSED);
        issuesMap.replace(hash,old,issue);
        return findOne(hash);
    }

    public Iterable<Issue> findAll(Issue.Status status) {
        Process theProcess;
        try {
            theProcess = Runtime.getRuntime().exec(GIT_BUG + "-a");
        } catch(IOException e) {
            throw new ExecutionFailedException();
        }
        HashSet<Issue> result = new HashSet<Issue>();
        String info;
        Issue issue;
        try (BufferedReader inStream = new BufferedReader(new InputStreamReader(theProcess.getInputStream()))) {
            while((info=inStream.readLine())!=null) {
                issue = findOne(info.substring(0,40));
                if(issue.getStatus()==status||status==null) {
                    result.add(issue);
                }
            }
        } catch(IOException e) {
            throw new ExecutionFailedException();
        }
        return result;
    }

    public void postComment(Comment comment, String hash) {
        issuesMap.get(hash).addComment(comment);
    }

    private class ExecutionFailedException extends RuntimeException {
    }
    public static class IssueNotImplementedException extends RuntimeException {
    }
}
