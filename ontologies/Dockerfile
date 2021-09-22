FROM python:3.9

RUN mkdir /opt/owlready2

WORKDIR /opt/owlready2
ENV PYTHONPATH /opt/owlready2
	  

COPY requirements.txt .

RUN git clone https://github.com/SINTEF-9012/SkyTrack.git

COPY . ./SkyTrack/ontologies
RUN apt-get update
RUN apt-get install -y openjdk-8-jdk
RUN apt-get install -y openjdk-8-jre

RUN pip install --no-cache-dir -r requirements.txt