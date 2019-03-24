## Pull Request Automator

To use, run Main.java with args:

1. Path to patch file.
2. Path to file that's going to be modified.
3. Local path to file that's going to be modified/
4. Base branch (branch to merge into).
5. New branch name.
6. Repo coordinates.
7. Username of user making pull request.
8. Path to the password of user making pull request.
9. Name of person making pull request.
10. Email of person making pull request.

Patch file must be in following format:

```STATUS: status of patch
MODIFIED: name of modified
CLEANER: name of cleaner
POLLUTER: name of polluter
...
==========================
Patch contents here
...
```