What is it?
===========

This is a plausibly simple ZMTP/1.0 library.

That is, it provides the ability to speak enough ZMTP to send messages to a ZeroMQ peer, and, one day, to read messages back from it, and it does this as simply as possible (not no more simply).

This library does not attempt to provide the alleged functionality of the ZeroMQ libraries. It doesn't do threading or buffering, or even reconnection. It doesn't integrate with any cool IO frameworks, or do anything cool at all, really. It just writes a ZMTP stream to a java.io.OutputStream. Which you can get from a java.net.Socket.

How do i build it?
==================

With Gradle (http://www.gradle.org/). To build, simply do::

    gradle clean build

This builds a jar file in ``build/libs``. To use this in other projects, you might like to install it in your local Maven repository::

    gradle install

How do i use it?
================

Something like::

    Socket socket = new Socket(host, port);
    MessageOutputStream out = new MessageOutputStream(new FrameOutputStream(new BufferedOutputStream(socket.getOutputStream())));
    out.write(message.getBytes());
    out.flush();
