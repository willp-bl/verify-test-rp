FROM govukverify/java8

WORKDIR /app

ADD configuration/local/test-rp.yml test-rp.yml
ADD build/distributions/ida-sample-rp-0.1.local.zip ida-sample-rp.zip

RUN unzip ida-sample-rp.zip

CMD ida-sample-rp-0.1.local/bin/ida-sample-rp server test-rp.yml 
