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

#### Q: Does the tag size change between L1 and L2 cache? Yes, right?

A: Of course it does.

#### Q: Can the L1C and L1D be combined or do they need to be separate?

A: They need to be separate, per the project doc.


#### Q: When we victimize the block do we need to return the data?

A: TBD

#### Q: What are the subsequent requests for data to L1D for in the 2nd/3rd write scenario for?

A: TBD

#### Q: How does the instantaneous miss scenario work?

A: ??

#### Q: How do we map the miss states to L1 cache. Tag can be anywhere?

A: ??

#### Q: What's the significance of consecutive writes to L1? (writing on step 4, 5)

A: ??


---
## General TODO Ideas
Instead of putting TODOs everywhere, consider putting a few in here.

* Write buffer needs to be implemented
* Improve the "full cache block" strategy
* I also want to consider validation, i.e. cannot have 10 bit index for 8KB cache


* So, I'm just not getting how the write buffer works.

* What does Drew think of the writeDataTriggeredByRead methods? 

* CacheBlock has tag, and not full address! WTF was I thinking?

* Umm, in all examples, cache block only has tag. So, how does it get translated to L2? Ssshh

* Need to handle transitions between L1 data/CacheBlock

* Need to test wiring up

* Need to address l2 Write buffer?!? not included in l2datastore

* Need to fix victim cache test.
  

  * Fix Level2Data Store used in L2 Data public DataResponseType canWriteToCache(Address address) {
  return DataResponseType.HIT; // TODO - FIXME
  }