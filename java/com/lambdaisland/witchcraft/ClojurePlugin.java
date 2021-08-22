package com.lambdaisland.witchcraft;

import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import clojure.lang.Compiler;
import org.bukkit.plugin.java.JavaPlugin;

public class ClojurePlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        ClassLoader prevCtxLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader myLoader = this.getClass().getClassLoader();

        getLogger().info("Setting PluginClassloader as Context classloader: " + myLoader.toString());

        try {
            Thread.currentThread().setContextClassLoader(myLoader);
            RT.var("clojure.core", "require") .invoke(Symbol.intern("lambdaisland.witchcraft.plugin"));
            RT.var("lambdaisland.witchcraft.plugin", "on-enable").invoke(this);
        } finally {
            Thread.currentThread().setContextClassLoader(prevCtxLoader);
        }
    }

    @Override
    public void onDisable() {
        RT.var("lambdaisland.witchcraft.plugin", "on-disable").invoke(this);
    }
}
