FROM govukverify/java8

WORKDIR /app

ADD configuration/local/test-rp.yml test-rp.yml
ADD build/distributions/verify-test-rp-0.1.local.zip verify-test-rp.zip

RUN unzip verify-test-rp.zip

CMD verify-test-rp-0.1.local/bin/verify-test-rp server test-rp.yml 
