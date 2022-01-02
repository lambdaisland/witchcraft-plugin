# 0.7.35 (2022-01-02 / 210b6a4)

## Added

- Softdepend on Citizens

# 0.5.27

## Added

- Support for Paper 1.18

## Changed

- Version bumps

# 0.1.22

## Fixed

- Prevent a race condition when starting nREPL

## Added

- introduce the :require config key

## Changed

- upgrade Witchcraft and lambdaisland.classpath
- The Witchcraft upgrade brings numerous small API improvements and bug fixes.

# 0.0.14

## Changed

- contains a better default template for the config and some other improvements
- bumps the witchcraft version

# 0.0.10

## Added

- This version upgrades the default version of Witchcraft it pulls in, and makes
  use of the new task-eval nREPL middleware in Witchcraft. This runs any code
  evaluated via the REPL on the game thread, which is a big quality of life
  improvement, since without it you need to wrap a lot of operations in (run-task
  ...)

# 0.0.7

## Added

- This is the first release of the Witchcraft Plugin, which adds Clojure support
  to Minecraft, and which accompanies the Witchcraft library.
  
- With this plugin you get an embedded nREPL server for interactive creative
  coding, deps.edn support to add additional dependencies, and the ability to load
  and evaluate Clojure code when the server starts.