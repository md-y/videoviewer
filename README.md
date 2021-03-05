# videoviewer
A Spigot plugin that allows video playback on entities.

[Download on Curseforge](https://www.curseforge.com/minecraft/bukkit-plugins/videoviewer-play-videos-in-minecraft)

# Demo
[Bad Apple demo on armor stands](https://www.youtube.com/watch?v=dAN99kQACi8)
[![Demo](https://i.ytimg.com/vi_webp/dAN99kQACi8/maxresdefault.webp)](https://www.youtube.com/watch?v=dAN99kQACi8)

# Usage
Videos located in the ```plugins/videoviewer``` directory can be played.

The ```/screen``` command is used for all functions.

Specifically, ```/screen create [width] [height] [entity type]``` creates a screen, and ```/screen play [filename]``` plays a video.
Use ```/screen select``` to select the nearest screen for playback.

# Building
The plugin uses [JavaCV](https://github.com/bytedeco/javacv) with shaded binaries. 

In order to minimize the plugin jar size, it is recommended that one of the builds scripts is used or that building is done following [this](https://github.com/bytedeco/javacpp-presets/wiki/Reducing-the-Number-of-Dependencies#include-binaries-only-for-one-or-more-platforms) article.

Or you can download from the [release page](https://github.com/md-y/videoviewer/releases).