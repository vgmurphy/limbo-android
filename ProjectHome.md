# Latest News #

  * **Oct 2014**: An R&D team from **Samsung** has rebased Limbo with Qemu 1.7 and further enhanced functionality and performance successfully **booting Windows 8.1 on an Android device with KVM**. Their work was presented in KVM 2014 Forum. Their presentation can be watched here: https://www.youtube.com/watch?v=1Ex7r5DEiNY

Slides:
http://events.linuxfoundation.org/sites/events/files/slides/KVMForum2014_BokdeukJeong_final.pdf

  * **Oct 2013**: A team from **Intel** has successfully enabled **KVM support** with Limbo for x86 Android devices. A technical presentation including a **demo running Windows 8 on an Android device** was given in the KVM 2013 forum:
Technical Presentation:
http://www.youtube.com/watch?v=sZQ-xZfc8NA

Slides:
https://docs.google.com/file/d/0Bx_UwXmBKWsyWnhRaFgxNDlfZlU

Intel's Demo running Windows 8 on an Android Tablet using Limbo:
https://docs.google.com/file/d/0Bx_UwXmBKWsyVS01WVBSM3FSM3M

  * I recommend you to **visit the YT video links above and give the Intel and Samsung teams your support in making these projects a reality**.

## What is Limbo? ##

  * Limbo is an x86 PC Emulator for Android devices based on [QEMU](http://wiki.qemu.org/).
  * Download source code [here](https://code.google.com/p/limbo-android/source/checkout).

## THIS PROJECT IS NO LONGER MAINTAINED ##
  * If you wish to continue work please fork


## Supported Host OSes: ##
  * Android ARM
  * Android x86

## Supported Guest OSes: ##
  * DSL Linux (ver 4.4.10 & 4.4.11 RC2) http://www.damnsmalllinux.org
  * Debian (Only 3.0.x Woody) http://cdimage.debian.org/cdimage/archive/
  * FreeDOS http://www.freedos.org/
  * Kolibri OS http://kolibrios.org/en/
  * AROS http://aros.sourceforge.net/

## WARNING ##
  * **MS WINDOWS, UBUNTU**, and other large OSes are **NOT** supported by the current version of Limbo because of their **heavy use of CPU, Memory, and SD Card**, as well as other incompatibilities.
  * Some Devices are known **NOT** to work with Limbo (ie Nexus 4).

## ISSUES ##
  * Limbo runs under emulation and **NOT** virtualization so it's slower than an emulator running on your PC. Virtualization is made possible with KVM, see Latest News.
  * Limbo needs several UI enhancements (Mouse, Gestures, etc..) as well as filesystem integration (file sharing) and QEMU core performance optimizations catered for Android OS.
  * Limbo is based on QEMU 1.1.0 which is an old version and needs to be re-based to newest [QEMU 1.7](http://wiki.qemu.org/Download). QEMU versions 1.5+ contain changes in the cpu timer routines which need to be ported successfully to Android in order for Limbo to work.

I'm hoping others will read this and continue to improve Limbo.

## README ##
  * For instructions on how to build Limbo for ARM or x86 Android devices see README in the root folder of the source code.

##  THIS PROJECT IS NO LONGER MAINTAINED 