{ pkgs ? import <nixpkgs> {config.android_sdk.accept_license = true; config.allowUnfree = true;} }:

let
  androidSdk = pkgs.androidenv.androidPkgs_9_0.androidsdk;
in
pkgs.mkShell {
  buildInputs = with pkgs; [
    androidSdk
    glibc
  ];
  # override the aapt2 that gradle uses with the nix-shipped version
  GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${androidSdk}/libexec/android-sdk/build-tools/32.0.0/aapt2";
}
