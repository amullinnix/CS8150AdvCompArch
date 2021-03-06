# CS8150AdvCompArch
CS8150 - Advanced Computer Architecture - Course Project

## Questions for the class
In this section, let's start logging some questions, so we can finally ask the 
professor at the beginning of class. It will make his day!

---

#### Q: In the project description, it talks about "CPURead A," "CPURead B," etc. Yet, in the beggining of class, we talked about tag, index, and offset. Do we need to actually calculate those, or just "fake" it with "A", "B", etc?

A: Yeah, we do. Definitely use real addresses, but they can be whatever form you want. Hex, decimal, binary, etc.

#### Q: Can this be done single threaded, as in each entity processes the next message?

A: TBD

#### Q: I'm not sure what is meant by implementing buses. We were going to simply have each controller put messages on the other controller's queues for processing. Can you elaborate a bit more? 

A: Didn't quite get this, but he said something about being able to ignore bus stuff

#### Q: Confirming, tag is not included in cache size, right?

A: Definitely not.

#### Q: Let's say you have a tag, and write one byte, same tag, different offset. Same cache block?

A: Pretty sure yes, let's find out.


---
## General TODO Ideas
Instead of putting TODOs everywhere, consider putting a few in here.

* Need to start thinking about generalizing cache so that L2 cache
can easily be created.
  
* Write buffer needs to be implemented
* Victim Cache needs to be implemented
* Improve the "full cache block" strategy