## Pull Request Automator

To use, run the command:

```aidl
java -cp PullRequestAutomator-1.0-SNAPSHOT.jar:dependency/*: com.example.Main
```

With args:

1. Path to patch file.
2. Path to file that's going to be modified.
3. Local path to file that's going to be modified.
4. Base branch (branch to merge into).
5. New branch name.
6. Repo coordinates (/SLUG)
7. Path to file containing credentials

Patch file must be in following format:

```
STATUS: status of patch
MODIFIED: name of modified
CLEANER: name of cleaner
POLLUTER: name of polluter
...
==========================
Patch contents here
...
```

Credentials file must be in the following format:

```
NAME: name
USER: username
PASSWORD: password
EMAIL: email
```