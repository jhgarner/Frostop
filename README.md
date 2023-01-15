# Frostop

Remote desktop software for Linux/X11 and Android. Use an Android tablet (or
phone) to control a (possibly headless) Linux computer.

The backend is written in rust and builds using standard cargo commands. Copy
and edit the config.toml to ~/.config/frostop/config.toml.

The frontend is an Android app written in Kotlin. Build using Android Studio or
gradle.

Instead of building authentication into the network protocol, this app relies on
something like wireguard to encrypt/protect your connection. For example, you
can generate public/private key pairs on your desktop and Android devices, share
the public keys, and make the devices into peers.

## System requirements

### Desktop

* Linux
* Xvfb
* VirtualGL (technically optional, but you'll be sad without it)
* An x11 window manager
* A Nvidia Gpu (There's no reason Intel and Amd can't be supported, but I only
  have Nvidia to test on. As long as you have hardware encoding and ffmpeg
  supports it, it should be workable. If you're interested in testing on a
  non-nvidia gpu, let me know)

### App

* Android (newer is better)
* Physical keyboard
* Stylus with hover (optional)

## Panels

The frontend uses panels to map touch controls to mouse/keyboard movements.
Using a stylus bypasses the panels, but using your finger doesn't. Panels are
defined using Json. You can see some examples in the discussion section on
Github. There's not really any validation right now, so if you mess up the panel
json, the app will probably crash when you try to connect. If that happens, just
reload the app and fix it.
