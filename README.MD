#create
```
PUT /search
{
  "settings":{
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
 },
 "mappings": {
    "SmartHome": {
      "properties": {
        "domain": {
          "type": "keyword"
        },
        "param": {
          "type": "text",
          "index": false,
          "store": true
        },
        "score": {
          "type": "long",
          "index": false,
          "store": true
        },
        "uuid": {
          "type": "keyword",
          "index": false,
          "store": true
        },
        "words": {
          "type": "text",
          "store": true,
          "analyzer": "ik_smart_syn"
        }
      }
    }
  }

}
```
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
