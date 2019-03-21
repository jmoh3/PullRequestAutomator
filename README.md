## Pull Request Automator

To use, run Main.java with args:

1. Path to patch file.
2. Base branch (branch to merge into).
3. New branch name.
4. Repo coordinates.
5. Username of user making pull request.
6. Path to the password of user making pull request.
7. Email of person making pull request.

Patch file must be in following format:

STATUS: status of patch
MODIFIED: name of modified
CLEANER: name of cleaner
POLLUTER: name of polluter
...
==========================
Patch contents here
...