# Command Optimiser

Reduces performance impact of command blocks by caching parsing results.

## How it works
When running command blocks, the game parses the command text every time it is run. This takes up a lot of redundant processing time. Instead if the results of parsing are stored in memory we can speed up command execution. The command only needs to be parsed again when it is changed.

## Expected performance improvements
Depending on the number of command blocks you use, the performance gains may vary.

You can safely expect that the TPS impact of command blocks will reduce by at least 10-20%. Gains will likely be smaller for commands that affect blocks than other, less intensive commands.

Testing with real-world command usage showed a reduction of about 25-30%
