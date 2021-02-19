# videoviewer
A Spigot plugin that allows video playback on entities.

# Usage
Videos located in the ```plugins/videoviewer``` directory can be played.

The ```/screen``` command is used for all functions.

# Building
The plugin uses javacv with shaded binaries. 

In order to minimize the plugin jar size, it is recommended that one of the builds scripts is used or that building is done following [this](https://github.com/bytedeco/javacpp-presets/wiki/Reducing-the-Number-of-Dependencies#include-binaries-only-for-one-or-more-platforms) article.