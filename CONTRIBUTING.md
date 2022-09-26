# Contribution Guidelines

**You want to contribute to the project ?** First, we thank you for taking your time!

But you should follow these guidelines, as it makes everyones time easier, including yours!

## Issues
Whenever you want to submit a **bug report** or a **feature request**, you should always submit it in the **Issues** tab.

## Bug Reports
You should **ALWAYS** follow the Bug Report Example whenever you are submitting one. If your Bug Report doesn't follow the example, we may not take it into consideration.

## Feature Requests
Feature Requests are always welcome. Before submitting one, please consider that not every idea fits the project, and we may reserve to refuse that request.

## Pull Requests (PR)
Pull Requests are always welcome.

PR should always be about an issue present in the issue tab. Whether it is a bug report, or an accepted Feature Request.

You should always ask before making a Feature Request PR, as we may want to not add it for the moment.
You should **always** adhere to the coding conventions the project uses, such as indentation, comments, etc...)

Here are the steps into making a good PR:

1. [Fork](http://help.github.com/fork-a-repo/) this repository, clone your fork, and configure the remotes.

   ```bash
   # Clone your forked repository
   git clone https://github.com/<username>/<repo_name>

   # Navigate to the folder repository
   cd repo_name

   # Assign the original repo as an upstream
   git remote add upstream https://github.com/chocololat/Mesalytic
   ```

2. Always get the latest changes (especially if you forked it a long time ago)

   ```bash
   git checkout <dev>
   git pull upstream <dev>
   ```

3. Create a new topic branch (off the main project development branch) to
   contain your feature, change, or fix:

   ```bash
   git checkout -b <topic-branch-name>
   ```

4. Commit your changes in logical chunks. Please adhere to these [git commit
   message guidelines](http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html)
   or your code is unlikely be merged into the main project. Use Git's
   [interactive rebase](https://help.github.com/articles/interactive-rebase)
   feature to tidy up your commits before making them public.

5. Locally merge (or rebase) the upstream development branch into your topic branch:

   ```bash
   git pull [--rebase] upstream <dev-branch>
   ```

6. Push your topic branch up to your fork:

   ```bash
   git push origin <topic-branch-name>
   ```

7. [Open a Pull Request](https://help.github.com/articles/using-pull-requests/)
    with a clear title and description.
