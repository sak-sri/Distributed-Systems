# Lamport Clock — Distributed System Simulation

A Java implementation of **Lamport's Logical Clock** algorithm across a fully connected peer-to-peer network of nodes, demonstrating causality tracking in distributed systems.

---

## What is a Lamport Clock?

A Lamport clock is a logical clock used in distributed systems to order events and establish causality. It follows two simple rules:

- **Send:** Increment clock before sending a message
- **Receive:** Set clock to `max(local, received) + 1`

This ensures that if event A causes event B, then `clock(A) < clock(B)`.

---

## Project Structure

```
├── Main.java           # Entry point — creates nodes and starts the simulation
├── Node.java           # Core node logic — Lamport clock, server/client threads
└── InputHandler.java   # Handles user input to send messages between nodes
```

---

## How It Works

1. **5 nodes** are created, each listening on a separate port (`5001`–`5005`)
2. Every node acts as both a **server** (accepts incoming connections) and a **client** (connects to all other nodes)
3. Once all connections are established, the user can send messages between any two nodes
4. Each message carries the sender's current clock value
5. The receiver updates its clock using `max(local, received) + 1` and sends an ACK

```
Node A (clock=2) ──── message (clock=2) ───► Node B (clock=max(1,2)+1 = 3)
                ◄─── ack (clock=3) ──────────
```

---

## Getting Started


## Usage

Once running, you will be prompted interactively:

```
Enter port from which you want to send
> 5001
Enter port to which you want to send
> 5003
Enter Message
> Hello from Node1
```

**Output:**
```
Node Name : Node1
Node Port : 5001
Operation : SEND
Current Clock : 0
Updated Clock : 1
Message Received : {"sender":"Node1","message":"Hello from Node1","myClock":1}
Node Name : Node3
Node Port : 5003
Operation : RECEIVE
Current Clock : 0
Updated Clock : 2
Response: I have received your message
```

---

## Key Design Decisions

| Concern                                                  | Solution |
|----------------------------------------------------------|---|
| Race conditions on clock                                 | `synchronized` on `updateClock()` |
| Java visibility across threads                           | `volatile` on `myClock` |
| Wait for servers before clients connect                  | `CountDownLatch` per node |
| Parallel connection establishment                        | Separate thread per client connection |
| Multiple simultaneous incoming connections               | New `ServerThread` per accepted socket |
| Wait for Connection Establishment Before Sending Message | `CountDownLatch` per node                         |




---

## Ports Used

| Node  | Port |
|-------|------|
| Node1 | 5001 |
| Node2 | 5002 |
| Node3 | 5003 |
| Node4 | 5004 |
| Node5 | 5005 |

---

## Concepts Demonstrated

- Lamport's logical clock algorithm
- TCP socket programming in Java
- Multithreaded client-server architecture
- Thread synchronization (`synchronized`, `volatile`, `CountDownLatch`)
- Fully connected peer-to-peer topology