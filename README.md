This implementation:

1.) Fetches the customer's watched movies from MemoryDB
2.) Creates a vector representation of the customer's movie ratings
3.) Fetches all other customers from MemoryDB
4.) Uses cosine similarity to find the most similar customer
5.) Returns recommended movies that the similar customer has watched but the original customer hasn't
