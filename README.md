# CS8150AdvCompArch
CS8150 - Advanced Computer Architecture - Course Project

#Table of Contents
1. [Author Information](#author-information)
1. [Basic Project Information](#basic-project-information)
2. [General TODO Ideas](#general-todo-ideas)
3. [Questions for the class](#questions-for-the-class)

## Author Information and Github Link

**Drew Mullinnnix** - mullinnix.drew@gmail.com (or through UNO email)

**Michael Kolakowski** - mkolakow@gmail.com (or through UNO email)

**Github:** https://github.com/amullinnix/CS8150AdvCompArch

## Basic Project Information
This section gives you basic information on "running" the project, so to speak. There are several 
parts to look at. The application code, the unit tests, the state machine tests, and the file reader
test.

All tests are located in the src/test folder. 

We recommend importing the project in your favorite IDE, but we can only confirm working with IntelliJ.

Then, after doing a gradle build, you should simply be able to right click on any test and run it.

Finally, also see the test method in CacheControllerTest. This will read actual commands from a file.

We essentially have three different types of tests for this project

1. Unit tests on all the domain objects - proving things like LRU and write merging
2. State Machine Tests - proving the state machines work
3. File Reader - read a command(s) and run it through the application 

Finally - we also recommend looking at the github link provided above. In doing so, you'll be able to
see the commit history and get a feel for how the project evolved over time.

Any questions - please contact the authors. Thanks! 

--- 

## General TODO Ideas
Here are future project enhancements we would make if we had more time.

* Validation and calculation for cache components. That is to say, provide a cache size and 
  block size and automatically calculate the tag, index, and offset.
  
* Log4J / SLF4J instead of using System.out

* Consider making DRAM a state machine instead of just a component.
  
* Need to handle transitions better between L1 data/CacheBlock

* Need to fix victim cache test.

* Fix Level2Data Store used in L2 Data public DataResponseType canWriteToCache(Address address) {
  return DataResponseType.HIT; // TODO - FIXME
  }
  
---

## Questions for the class
In this section, let's start logging some questions, so we can finally ask the 
professor at the beginning of class. 

#### Q: In the project description, it talks about "CPURead A," "CPURead B," etc. Yet, in the beggining of class, we talked about tag, index, and offset. Do we need to actually calculate those, or just "fake" it with "A", "B", etc?

A: Yeah, we do. Definitely use real addresses, but they can be whatever form you want. Hex, decimal, binary, etc.

#### Q: Can this be done single threaded, as in each entity processes the next message?

A: Per discussion, essentially yes. Can use pipelining or however we implement state machine.

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

A: Yes.

#### Q: What are the subsequent requests for data to L1D for in the 2nd/3rd write scenario for?

A: Write the data block fetched from lower level first, then write the new data to the block.

#### Q: How does the instantaneous miss scenario work?

A: ??

#### Q: How do we map the miss states to L1 cache. Tag can be anywhere?

A: ??

#### Q: What's the significance of consecutive writes to L1? (writing on step 4, 5)

A: Again, writing block first, then modifications


---
