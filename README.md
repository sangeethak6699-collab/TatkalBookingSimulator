## Project Description

An industry-style Java application simulating concurrent user ticket booking during peak Tatkal timing, demonstrating multi-threading, synchronization, custom exception handling, and GUI design. When Tatkal booking opens, thousands of users try to book a limited number of train seats simultaneously. Without proper thread synchronization, multiple users can end up with the same seat (race condition). This project implements a thread-safe Tatkal simulator in Java that correctly allocates Confirmed, RAC, and Waiting-List (WL) tickets under high concurrency.

## Features

* Multithreaded concurrent booking engine
* Thread synchronization (`synchronized` locks) to prevent race conditions
* Queue-based waiting list management (`ConcurrentLinkedQueue`)
* Custom exception handling (`SeatNotAvailableException`)
* Real-time Swing Graphical User Interface (GUI) dashboard
* Unique PNR generation and status tracking

## Technologies Used

* Java (JDK 8+)
* Java Swing & AWT (GUI Interface)
* Multithreading & Synchronization
