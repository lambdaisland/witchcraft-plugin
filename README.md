# witchcraft-plugin

<!-- badges -->
[![cljdoc badge](https://cljdoc.org/badge/com.lambdaisland/witchcraft-plugin)](https://cljdoc.org/d/com.lambdaisland/witchcraft-plugin) [![Clojars Project](https://img.shields.io/clojars/v/com.lambdaisland/witchcraft-plugin.svg)](https://clojars.org/com.lambdaisland/witchcraft-plugin)
<!-- /badges -->

A Clojure plugin for Bukkit-based minecraft servers (Paper, Spigot, CraftBukkit, Glowstone)

## Installation

To build the plugin you need a recent version of Clojure CLI, see the [Clojure getting started](https://clojure.org/guides/getting_started) guide

```
$ clojure --version
Clojure CLI version 1.10.3.943
```

Then use the build-plugin task, you need to tell it which server you will be
using, and the API version. Currently only PaperMC 1.17 has been tested.

```
clojure -T:build-plugin build :server paper :api-version 1.17 
```

Copy the plugin jar to your server's `plugins` directory

```
$ cp target/witchcraft-plugin-0.0.3-for-paper-1.17.jar ~/PaperMC/plugins/
```

The first time you run the server it will create a `deps.edn` and
`plugins/witchcraft.edn`. It will also start an nREPL server, and invoke any
`:init` commands you have in your `witchcraft.edn`.

```
[09:33:14 INFO]: [Witchcraft] Enabling Witchcraft v0.0.3
[09:33:14 INFO]: [Witchcraft] Setting PluginClassloader as Context classloader: PluginClassLoader{plugin=Witchcraft v0.0.3, pluginEnabled=true, url=plugins/witchcraft-plugin-0.0.3-for-paper-1.17.jar}
[09:33:26 INFO]: [Witchcraft] No plugins/witchcraft.edn found, creating default.
[09:33:26 INFO]: [Witchcraft] No deps.edn found, creating default.
[09:33:29 INFO]: [Witchcraft] init: (require (quote lambdaisland.witchcraft))
[09:33:30 INFO]: [Witchcraft] init: (lambdaisland.witchcraft/init-xmaterial!)
[09:33:36 INFO]: nREPL server started on port 25555 on host localhost - nrepl://localhost:25555
```

The plugin JAR bundles Clojure, nREPL, and clojure.tools.deps. Any additional
dependencies (e.g. nREPL middleware, Witchcraft, clj-minecraft) can be loaded
via `deps.edn`.

### witchcraft.edn

```clojure
{:nrepl
 {:port 25555
  :middleware [refactor-nrepl.middleware/wrap-refactor
               cider.nrepl/cider-middleware]}
 :init [(require 'lambdaisland.witchcraft)
        (lambdaisland.witchcraft/init-xmaterial!)]
 :deps true}
```

- `:nrepl` are options passed to `nrepl.cmdline/dispatch-commands`, if you don't
  want to run an nREPL server, use `:nrepl false`
- `:deps` can be `true` (load dependencies from `deps.edn`), `false` (don't), or
  a map with options for tools.deps, e.g. `:deps {:aliases [:my-alias]}`
- `:init` is a sequence of Clojure forms that are evaluated after starting the
  server. You can use this to load additional Clojure modifications when booting
  the server
  
## deps.edn

See the Clojure documentation for how to set up `deps.edn`. The default version
will load Witchcraft, as well cider-nrepl and refactor-nrepl.

## Usage

Now you can connect with your editor to port 25555 and start manipulating the
game. Or you can create modifications in Clojure and load them up via `:init`.

<!-- opencollective -->
## Lambda Island Open Source

<img align="left" src="https://github.com/lambdaisland/open-source/raw/master/artwork/lighthouse_readme.png">

&nbsp;

witchcraft-plugin is part of a growing collection of quality Clojure libraries created and maintained
by the fine folks at [Gaiwan](https://gaiwan.co).

Pay it forward by [becoming a backer on our Open Collective](http://opencollective.com/lambda-island),
so that we may continue to enjoy a thriving Clojure ecosystem.

You can find an overview of our projects at [lambdaisland/open-source](https://github.com/lambdaisland/open-source).

&nbsp;

&nbsp;
<!-- /opencollective -->

<!-- contributing -->
## Contributing

Everyone has a right to submit patches to witchcraft-plugin, and thus become a contributor.

Contributors MUST

- adhere to the [LambdaIsland Clojure Style Guide](https://nextjournal.com/lambdaisland/clojure-style-guide)
- write patches that solve a problem. Start by stating the problem, then supply a minimal solution. `*`
- agree to license their contributions as MPL 2.0.
- not break the contract with downstream consumers. `**`
- not break the tests.

Contributors SHOULD

- update the CHANGELOG and README.
- add tests for new functionality.

If you submit a pull request that adheres to these rules, then it will almost
certainly be merged immediately. However some things may require more
consideration. If you add new dependencies, or significantly increase the API
surface, then we need to decide if these changes are in line with the project's
goals. In this case you can start by [writing a pitch](https://nextjournal.com/lambdaisland/pitch-template),
and collecting feedback on it.

`*` This goes for features too, a feature needs to solve a problem. State the problem it solves, then supply a minimal solution.

`**` As long as this project has not seen a public release (i.e. is not on Clojars)
we may still consider making breaking changes, if there is consensus that the
changes are justified.
<!-- /contributing -->

<!-- license -->
## License

Copyright &copy; 2021 Arne Brasseur and Contributors

Licensed under the term of the Mozilla Public License 2.0, see LICENSE.
<!-- /license -->
