const fs = require('fs');

const graphy = require('graphy');
const nt_read = graphy.content.nt.read;
const nq_read = graphy.content.nq.read;
const ttl_read = graphy.content.ttl.read;
const trig_read = graphy.content.trig.read;


fs.createReadStream('../ontologies/vicodi_all.owl')
    .pipe(ttl_read())
    .on('data', (y_quad) => {
       console.dir(y_quad.isolate());
    })
    .on('eof', () => {
        console.log('done!');
    });