## Via Web Frontend
System setup, at least two instances (one for the SUT and another for openISBT). 
If your benchmark client (the openISBT instance) becomes a bottleneck, 
you're benchmarking the client and should start at least one more instance with some more workers.

<img src="doc/overview.png" alt="Overview" width="600"/>

----

## Setup

### SUT
1. Setup your REST service and note the url.
2. Find or generate the openAPI3.0 description file.

### OpenISBT
0. Connect to (EC2) instance
1. Install git: `sudo yum install git -y`
2. Clone openISBT repository: `git clone https://github.com/martingrambow/openISBT.git`
3. cd to openISBT: `cd openISBT`
4. Make *.sh files executable: `chmod +x *.sh`
5. Create swap (required for t2.micro instances): `./initSwap.sh swap1`

   Create more swap: `./initSwap.sh swap2`
6. run setup script: `./startOpenISBT.sh`
 
   (this script will also start one worker per default)


----

## Benchmark 
(with interactive browser GUI)

1. Click *Start*

### Paste OpenAPI 3.0 specification file
<img src="doc/start.PNG" alt="openAPI file" width="600"/>

1. Enter an URL, select one of the default files, or copy and paste your OpenAPI 3.0 file directly (e.g., pick petstore and click load).
2. Click *Next*

Alternatively, you can also upload an already created workload and skip the next steps.

### Define Workload
<img src="doc/pattern.PNG" alt="define patterns" width="600"/>

1. Load a default configuration and adjust the workload to your needs (e.g., pick the experiment workload)
2. Click *Next*

### Adjust Matching
<img src="doc/check.PNG" alt="check matching" width="600"/>

1. OpenISBT matches the openAPI file and workload
2. Check the matching by clicking on the pattern name (e.g., UPD)
3. Adjust the benchmarked paths (or go back to adjust the specification)
4. Click *Next*

### Generate Workload
<img src="doc/generate.PNG" alt="generate workload" width="600"/>

1. Click (Re-)generate workload
2. OpenISBT now generates the workload, this can take a while
3. Check the workload by clicking on a pattern request item
4. Click *Next*

### Define Workers and run benchmark
<img src="doc/run.PNG" alt="run benchmark" width="600"/>

1. Adjust the service endpoint (if neccessary)
2. Adjust the number of running threads per worker (if neccessary)
3. Configure at least worker (per default, there is one worker running at *localhost:8000*)
4. Check the worker status by clicking on *refresh worker status*, the status should switch to *W*(aiting)
5. Start the benchmark
6. OpenISBT runs the benchmark, you should see status messages
7. Wait for all workers to complete
8. Click *Next*

### Get Results
<img src="doc/results.PNG" alt="results" width="600"/>

1. Look at the results for each pattern, path and operation
2. Download the results as json file
