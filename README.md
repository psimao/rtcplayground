# RTC Playground

Sample project of an Android WebRTC implementation 🚀

Features:
* Video and Voice call between different devices
* Real time message and location shared between peers

## How does it work?

Before getting started with this project, it highly recommend you to read [this article](http://webrtc-security.github.io/) if you aren't familiarized with WebRTC terms and architecture.

##### Connecting
1) The app connects to a signalling server (web socket)
2) Become available to incoming offers and receives the list of other available users

##### Receiving a Call
1) Signalling server will emit an **incoming** event
2) After accepting the app emits a **call-answered** back
3) When it receives the **ready** event, a PeerConnection and a local stream objects are created
4) The next event emitted by the signalling server is an **offer** from the remote Peer with it's SDP
5) The remote SDP is set as remote description in PeerConnection Object
6) An **answer** is created, set as local description and emitted to the signalling server
7) **candidate** events will start to be shared between the peers and the signalling server containing the Ice Candidates of you RTC Connection
8) The remote stream and data channel come as Ice Candidates and set to PeerConnection
9) After receiving all the remote candidates, the connection is stablished!

##### Starting a Call
1) The app emits a **call** event to the signalling server with the target user id
2) The **created** event indicates when the room is created
3) When it receives the **ready** event, a PeerConnection, a local stream and the data channel objects are created
4) An **offer** is created, set as local description in PeerConnection and emitted to the signalling server
5) The **answer** event contains the remote sdp which is set in PeerConnection
6) **candidate** events will start to be shared between the peers and the signalling server containing the Ice Candidates of you RTC Connection
7) The remote stream comes as an Ice Candidate and set on PeerConnection
8) After receiving all the remote candidates, the connection is stablished! 

##### Closing
1) Emit an **bye** event to the signalling server to finish the call
2) Emit a **leave** event to the signalling server to disconnect from the socket

## Setup

It's using a local hosted web socket as Signalling server (You can check it out [here](https://github.com/plcart/webrtc-socket-sample)) and Google Maps API for location sharing and viewing between peers (More info [here](https://developers.google.com/maps/documentation/android-sdk/signup)).

Also:

* Android Studio 3.1.+.
* Setup an **app.properties** file in **app** folder (Follow **app/app.properties.sample** structure)
* Run

## License
```
MIT License

Copyright (c) 2018 Pedro Arthur Simão

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
