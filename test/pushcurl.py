#!/usr/bin/env python

"""
  this file provide an simple command line post request equivalent to curl.

  Usage: ./pushcurl.py url data
"""

import json
import base64
import requests
from optparse import OptionParser

#
# cat /tmp/push | POST -sedU -H 'Content-Length: 133'
#   -H 'Authorization: Basic FEBhVt9sCg2Xf1SQ2tWW2jhzgK4rQVss0qUmgfFPUQVIsGFiwznN8AxJ9Thx4x7HZY1vZgq929S86lZRC9EzMg=='
#   -c 'application/json' http://elephant-dev.locationlabs.com/application/v2/3Kfil21grTLOj3dQD0M7R8iz1Mo=
#
def curlpush(url, data):
    ''' execute curl command in the following format
        curl --header "authorization:Basic clid-a-31687-3" -d 'hello puid-a-31687-3!' http://elephant-dev.locationlabs.com/api/application/v1/puid-a-31687-3
    '''
    
    pushmsg = {
        "message": "hello world",
        "callback": {
            "url": "https://example-wrong.com",
            "username": "user",
            "password": "password"
        }
    }
    
    msgstr = json.dumps(data)
    print 'posting to :', url, ' data: ', msgstr

    headers = {}
    headers['authorization'] = 'Basic '+ base64.b64encode("default:secret")
    headers['content-type'] = 'application/json'
    headers['accept'] = 'application/json'

    req = requests.post(url, data=msgstr, headers=headers, verify=False)
    print 'response : ', req, req.text


if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option('-e', '--elephant', action='count', dest='elephant', default=0,
                      help='connect to elephant server')

    parser.add_option('-l', '--localserver', action='count', dest='local', default=0,
                      help='connect to local server')

    options, args = parser.parse_args()

    if len(args) < 1:
        print 'Usage: ./pushcurl.py endpoint data'
        exit(1)

    url = "http://localhost:8080"/args[0]
    if len(args) < 2:
        data = {
            "weekdays": ["SU", "TU"],
            "hour": 15,
            "minute": 30
        }
        
    else:
        data = args[1]


    print 'post data to ', url, data
    curlpush(url, data)
