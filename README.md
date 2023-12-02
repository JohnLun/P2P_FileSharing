# P2P_FileSharing - Group 12
 Rishi Sajay (rishisajay@ufl.edu)
 
 Jonathan Lun (jlun@ufl.edu) 

 Chase Wrenn (chasewrenn@ufl.edu)

 Max LoGalbo (mlogalbo@ufl.edu)

## Brief Overview

The following program initiates a Peer-to-Peer connection with X  neighbors who have started the same process and ensures that all neighbors who have started the process receive the targetted file by the end of the program.

## Contributions

Rishi Sajay - 

John Lun - Worked on preferred neighbor + optimistically unchoked handeling, logging, termination of program. Helped with the majority of debugging.

Chase Wrenn - Focus on implementing methods relating to processing messages. Also implemented OptimisticallyUnchokedNeighbor functionality, assisted with terminating application, and functionality relating to recognizing neighbors recieved files.

Max LoGalbo - Max was able to initiate debugging testing of the overall program, ensuring Linux connections from local PC's were working and helping out with overall debugging techniques.


## Demo Link

INSERT DEMO LINK

Please note - we were able to achieve all requirements detailed within the description of the project.


## How to Run

Once the zipped file is downloaded, import the unzipped folder into a Linux machine.

In the main directory (P2P_Filesharing), move to the directory "src"

Within src, place your designated files (Common.cfg, InfoPeer.cfg) along with the file you intend to send.

To compile all .java files in the "src" directory, run within this directory:
```
javac *.java
```

To run the program, use the command 
```
java peerProcess [this_peer_id]
```





