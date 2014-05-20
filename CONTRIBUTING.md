Contributing to AntiAdvertiser
==============================

Hi there! Glad to hear you're willing to help out with AntiAdvertiser. Please take some time to read this page before creating an issue report or a pull request.

Reporting an issue
------------------
So you've found a bug in AntiAdvertiser. What do you do?

To report an issue, use our [issue tracker](https://github.com/MrEinStain/AntiAdvertiser/issues/new). The title should briefly describe the issue you're experiencing (don't name it "I found a bug", describe the bug). You are expected to use the following template when reporting an issue:

```markdown
**Version of AntiAdvertiser:** (paste the output of "/version AntiAdvertiser" here)

**Version of Bukkit:** (paste the output of "/version" here)

**Steps to reproduce the bug:**
* Step one
* Step two
* ...

**What did you expect to happen?**

**What actually happened?**

(Any extra information here)
```

An example of a good (although invalid, since the plugin is just misconfigured in the config.yml) issue report with the title "IPs not blocked on signs":

> **Version of AntiAdvertiser:** 1.0
>
> **Version of Bukkit:** git-Spigot-1357
>
> **Steps to reproduce the bug:**
> * Place a sign
> * Put an IP address on the sign
>
> **What did you expect to happen?** The IP should have been blocked.
>
> **What actually happened?** The IP appeared on the sign and the advertiser wasn't kicked.
>
> My config.yml: http://pastebin.com/G4d9bbaC

Properly formatted issue reports mean we can spend more time fixing bugs and less time figuring out what the problem actually is.

Making a feature request
------------------------
If you feel AntiAdvertiser is lacking a feature, you can submit a feature request.

Just like with bug reports, use the [issue tracker](https://github.com/MrEinStain/AntiAdvertiser/issues/new) to submit feature requests. The title should briefly describe what you'd like added. Make sure to explain **in detail** what you'd like us to add and **justify**. Why would the feature be suitable for AntiAdvertiser?

Forking and making a Pull Request
---------------------------------
If you would like to and have the know-how, you may contribute by making a pull request to fix a bug or implement a feature.

To fork the repository, click the "Fork" button in the top right corner. This will create a copy of AntiAdvertiser in your GitHub account. Then import the newly created fork to your IDE of choice, make desired changes, commit and push them.

To make the pull request, navigate to https://github.com/(your_github_name)/AntiAdvertiser/compare/, select the commits to include in the PR and click "Create Pull Request". This will notify me and allow me to accept your code into the master branch, ask for clarification or deny the PR.