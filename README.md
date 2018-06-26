# MafiaScum Post/PM Reformatter
This repo hosts the code used on https://forum.mafiascum.net

# Usage
docker run --rm <other docker options> <image ID> [-f fileName] [-h databaseHost] [-u databaseUser] [-s databaseSchema] [-p databasePassword] [-r databasePort] [-b batchSize] [-t reformatter]

## Example
docker run 11802964a6b9 -f entities.txt -h 172.17.0.1 -r 3306 -p password -u root -s ms_phpbb3 -b 100000 -t QUOTE

## Arguments
```f: The name of the input file```

```h: The hostname or IP of the database server(default 127.0.0.1)```

```u: The database username```

```s: The database schema name```

```p: The database password```

```r: The database port (default 3306)```

```b: The size of the batched operations (default 10000)```

```t: The reformatter to be used (possible values: QUOTE)```

# Input file format
The input file should be a tab-delimited file with two columns. The first column should specify the entity type(POST or PM) while the second column has the entity ID. The file should not have a header row.

