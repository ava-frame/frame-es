#settings
```
 PUT /search/_settings
 {
     "index": {
       "analysis": {
         "analyzer": {
           "ik_smart_syn": {
             "type": "custom",
             "use_smart": "true",
             "tokenizer": "ik_smart",
             "filter": ["synonym"]
           }
         },
         "filter": {
           "synonym": {
             "type": "synonym",
             "synonyms_path": "analysis/synonym.txt"
           }
         }
         }
     }
 }

```
# frame-es
