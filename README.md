# Genetic algorithm protein folding using MPJ
This project was developed for a genetic algorithm course at the "Hochschule Darmstadt". \
It was extended with MPJ to allow execution on High performance clusters.



The input is a string of 0s and 1s. 
The goal is to fold the sequence optimizing the n4 neighbors of the 1s without an overlap in the chain.
All this happens on a 2D plain with 3 possible directions: straight, left, right.

## Possible settings

In the default options (`org.hda.gaf.DefaultOptions`) there are several settings you can modify/comment in:
* Choose one of the genetic algorithms:
    * Time limited algorithm (e.g. generate for 20s, then stop) 
    * Generation limited algorithm (e.g. generate 100 generations, then stop)
* Selection algorithms:
    * Fitness proportional (Every individual has a chance to get chosen. A higher fitness results in a better chance to get chosen)
    * Tunier fitness proportional (Same as fitness proportional but pairing individuals in a tunier and choosing every turn)
    * Tunier best fitness (Pairing individuals in a tunier and choosing the one with the best fitness)
* Population amount: Individual amount per generation
* Mutate rate: 
    Percent of how many genes of the total population should be mutated after selection. 
    The mutation includes changing a left turn to either a straight, right or left turn.
    
    E.g. Having 100 genes per individual in a population with 10 individuals means we have a total of 1000 genes. 
    With a mutation rate of 2% a total 20 of the genes gets modified every generation.
* Crossover rate: 
    Percent of how many individuals in a population should do a crossover every generation.
    Using a simple one point crossover (splitting the gene chain anywhere and switching it with another partner in the population).
    
    E.g. Having a population of 100 individuals and a crossover rate of 20% means that 20 individuals crossover every generation.
 
* Print while generating:
    Activates a GUI displaying the currently best found protein and some stats. For better performance deactivate it.
    
* MPJ population exchange times:
    How many times should the MPJ processes share part of their current population with another MPJ process.
    
    E.g. Generation amount is set to 100 and the MPJ exchange time to 5.
    That means every 20th generation the processes exchange 1/5th of their population with another process.

## Other projects
Base: \
This is the base implementation of the protein folding project.
It features a GUI displaying current stats and can be executed on a single machine. \
https://github.com/MPritsch/genetic-algorithm-protein-folding

Java Multithreading: \
Using multiple java threads to share the workload.
Exchanging part of the generated population with other threads to allow for more variety. \
https://github.com/MPritsch/genetic-algorithm-protein-folding-multithread

## Installing and using MPJ
For detailed MPJ documentation visit http://mpj-express.org/docs/guides/linuxguide.pdf

```
wget https://sourceforge.net/projects/mpjexpress/files/releases/mpj-v0_44.tar.gz
tar -xvf mpj-v0_44.tar.gz
mv mpj-v0_44 ~/MPJ
```

### Add maven dependency
This project needs an MPJ jar locally installed on maven. You can do that by executing following file: \
`./add-mpj-jar.sh`

### Generate jar
You can package this project via maven:\
`mvn clean package`
The jar should then be available at `target/genetic-algorithm-protein-folding-mpj-1.0-SNAPSHOT-jar-with-dependencies.jar`

### Local MPJ environment variables
On Bash add this to the top of your ~/.bash_profile file:
```
export MPJ_HOME=~/MPJ
export PATH=$MPJ_HOME/bin:$PATH
```

On Fish execute the following commands:
```
set -Ux MPJ_HOME ~/MPJ
set -U fish_user_paths $MPJ_HOME $fish_user_paths

# check they are set
echo $fish_user_paths | tr " " "\n" | nl
```




### Cluster setup

On Ubuntu: Add MPJ_HOME and JAVA_HOME environment variables to the start of your ~/.bashrc or ~/.bash_profile file 
(see [Local MPJ environment variables](#local-mpj-environment-variables)).
If that's not enough use the workaround mentioned for FreeBSD.

On FreeBSD: ssh environments needs to be enabled on all desired nodes. Ask your cluster admin to help you out.

When this option is enabled the login config of /etc/login.conf is bypassed. 
This means that all default paths like /bin /sbin /usr/bin etc. will not be configured. 
To fix this add the following lines to ~/.ssh/environment file and substitute your mpj and java paths:

```
MPJ_HOME=</path/to/mpj-installation>
JAVA_HOME=</path/to/java-jdk>

# On FreeBSD
PATH=/sbin:/bin:/usr/sbin:/usr/bin:/usr/local/sbin:/usr/local/bin:~/bin:$MPJ_HOME/bin
# On Ubuntu
PATH=$MPJ_HOME/bin:$PATH
```

Create a machines file. Add the addresses of your nodes to it (separated by newlines). \
`nano machines`

Startup MPJ Daemon on clusters with the machine files. \
`mpjboot <machines-file>`

Move your jar to the cluster. Execute the program on cluster.
You must be in the same directory as your machine file: \
`mpjrun.sh -np 4 -dev niodev -jar <jar-path>`

With slurm:\
`salloc -N 2 -n 4 mpjrun.sh -np 4 -dev niodev -jar <jar-path>`


### Local Execution on IntelliJ:
Edit your configuration to feature the following settings:

Main Class: `runtime.starter.MPJRun` \
VM Options: `-jar /home/marcus/MPJ/lib/starter.jar -np 4 org.hda.gaf.Main` \
Environment variables: `MPJ_HOME=/absolute/path/to/home/folder/MPJ`

To allow more java Heap space you can substitute these in VM options: \
`-jar /home/marcus/MPJ/lib/starter.jar -Xms4g -Xmx4g -np 4 org.hda.gaf.Main`

## Execution
You need to be in the same folder as your MPJ machine file for the commands to work.

Following command line options available:

Use default options 'generation' with the amount of generations defined in `org.hda.gaf.DefaultOptions`: \
`mpjrun.sh -np 4 -jar <jar-path>`
 
Specify total time spend per process: \
`mpjrun.sh -np 4 -jar <jar-path> --time <time in ms>`
 
Specify total generation amount per process: \
`mpjrun.sh -np 4 -jar <jar-path> --generation <generations>`
