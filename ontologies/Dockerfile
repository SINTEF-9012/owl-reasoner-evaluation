FROM python:3.9

RUN mkdir /opt/owlready2

WORKDIR /opt/owlready2
ENV PYTHONPATH /opt/owlready2
	  

COPY requirements.txt .

RUN git clone https://github.com/SINTEF-9012/SkyTrack.git

RUN apt update
RUN apt install -y default-jre

COPY . ./SkyTrack/ontologies

RUN pip install --no-cache-dir -r requirements.txt