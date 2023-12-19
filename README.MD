# Cat Config MC

***

### What is it ?

Cat Config MC is an adaptation of my existing library [Cat Config](https://github.com/LGatodu47/CatConfig)
for Minecraft mods and especially mods on fabric (as there is no integrated configuration like in Forge).
You can use this library in your fabric projects by adding its artifacts located at `io.github.lgatodu47.catconfig-mc`
on `https://s01.oss.sonatype.org/content/repositories/releases`. Here is a code snippet of a `build.gradle`
file that integrates the library:
```
repositories {
    [...]
    maven {
        name 'Sonatype Repositories'
        url 'https://s01.oss.sonatype.org/content/repositories/releases'
    }
}

dependencies {
    [...]
    def catConfigMCDependency = "io.github.lgatodu47:catconfig-mc:${mc_version}-0.2.1"
    modImplementation catConfigMCDependency
    include catConfigMCDependency
}
```
The artifacts aren't 'mod' files and therefore should be nested in your mod's jar. That is why we add the
dependency on two configurations here, on 'modImplementation' which allows it to use it in our source code
and on 'include' which will include the library's remapped jar into your mod's remapped jar.
Note that the property 'mc_version' should match the minecraft version you're on.

### How to use it ?

In this project, you can note the presence of two child modules. The first one, 'Source', corresponds to
the source code of this project, as the parent module has no 'src' directory. The second module, named
'TestMod' for now, is a mod that I used to test the features and that you may use for your implementation
in your mod. Feel free to take a look at the classes I've written (with no javadoc for now).

But for those who don't want to lose time, I'm gonna briefly explain the three main points of this library:
- First, we have an implementation of ConfigSide, MinecraftConfigSides, an enum with the three main
configuration sides of Minecraft: CLIENT, COMMON and SERVER.
- Next, we have the RenderedConfigOptions. To make it simple, these are objects that hold stuff about
how to represent config options in GUIs. You can easily create them using a RenderedConfigOptionBuilder
with its whole set of prebuilt widget factories.
- Finally, this library adds a configuration screen for your mod, that takes a list of RenderedConfigOptions
and a config on creation. You can create this screen whenever you want (in my TestMod module I have an
example of a Mod Menu implementation)

***

### So that's about it.

Just like Cat Config, Cat Config MC is still in development (version 0.*) and is in very early stages.
If you find a bug or have ideas for improvements, feel free to [open an issue](https://github.com/LGatodu47/CatConfigMC/issues)
or [a pull request](https://github.com/LGatodu47/CatConfigMC/pulls).