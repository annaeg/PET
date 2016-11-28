package experiments;

import ERMR.Configuration;
import ERMR.ERMRConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import configuration.Constants;
import entities.*;
import model.ExtractionResult;
import model.KeyValueResult;
import model.Part;
import models.CoreModel;
import models.ProcessModel;
import relations.DEMRelation;
import relations.RelationBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This will be the Mediator Script of the PERICLES in Practice Session.
 */
public class MediatorScript_PiP_Session1 extends Experiment {

    public MediatorScript_PiP_Session1() {
        super("MediatorScript_PiP_Session1");
        createScenarioTemplate();
    }

    ////////// TEMPLATE //////////// >>>

    private Community weatherScientists;
    private ServiceInterface webInterface;
    private TechnicalService dataService;

    private List<HumanAgent> scientists = new ArrayList<HumanAgent>();
    private List<DigitalObject> datasets = new ArrayList<DigitalObject>();

    private DEMRelation lastModified;
    private DEMRelation fileType;
    private DEMRelation fileSize;

    private void createScenarioTemplate() {
        weatherScientists = new Community(scenario, "Weather Scientists");
        webInterface = new ServiceInterface(scenario, "EUMETSAT Web Portal");
        dataService = new TechnicalService(scenario, "EUMETSAT Data Service");
        webInterface.providesAccessTo(dataService);
        webInterface.isUsedBy(weatherScientists);
        lastModified = new RelationBuilder(scenario, "last modified", CoreModel.digitalObject).create();
        fileType = new RelationBuilder(scenario, "file type", CoreModel.digitalObject).create();
        fileSize = new RelationBuilder(scenario, "file size", CoreModel.digitalObject).create();
    }

    private HumanAgent createScientist(String name) {
        HumanAgent scientist = new HumanAgent(scenario, name);
        scientist.isMemberOf(weatherScientists);
        return scientist;
    }

    private DigitalObject createDataset(String name, String last, String type, String size, HumanAgent scientist) {
        DigitalObject dataset = new DigitalObject(scenario, name);
        dataset.addProperty(lastModified, last);
        dataset.addProperty(fileType, type);
        dataset.addProperty(fileSize, size);
        dataset.storedOn(dataService);
        dataset.hasAuthor(scientist);
        return dataset;
    }

    ////////// INFORMATION MAPPING //////////// >>>

    /**
     * This method receives the information extracted by PET.
     * @param rawData
     */
    public void receivePETData(String rawData) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try {
            Part data = mapper.readValue(rawData, Part.class);
            mapPETData(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendToERMR();
    }

    /**
     * This method inserts the extracted information into the template to create Digital Ecosystem Model entities.
     * @param data
     */
    private void mapPETData(Part data){
        DigitalObject dataset = getDataset(data.fileName);
        for (ExtractionResult result : data.extractionResults) {
            if (result.moduleName.equals("Posix file information monitoring")) {
                KeyValueResult kvResult = (KeyValueResult) result.results;
                dataset.addProperty(lastModified, kvResult.results.get("last_modified"));
                dataset.addProperty(fileSize, kvResult.results.get("file_size"));
                dataset.addProperty(fileType, kvResult.results.get("file_type"));

                HumanAgent scientist = getScientist(kvResult.results.get("file_owner"));
                dataset.addProperty(ProcessModel.hasAuthor, scientist);
                // TODO: check that there are no duplicates
            }
        }
    }

    /**
     * Returns Digital Object if already existing, or creates new Digital Object otherwise.
     * @param fileName
     * @return
     */
    private DigitalObject getDataset(String fileName){
        for(DigitalObject dataset : datasets){
            if(dataset.name.equals(fileName)){
                return dataset;
            }
        }
        DigitalObject dataset = new DigitalObject(scenario, fileName);
        datasets.add(dataset);
        return dataset;
    }

    private HumanAgent getScientist(String name){
        for(HumanAgent scientist : scientists){
            if(scientist.name.equals(name)){
                return scientist;
            }
        }
        HumanAgent scientist = new HumanAgent(scenario, name);
        scientists.add(scientist);
        weatherScientists.hasMember(scientist);
        return scientist;
    }


    private void sendToERMR() {
        String directory = Constants.PROJECT_HOME;
        File file = new File(directory + File.separator + CoreModel.sanitizeName("EcoBuilder_DEMO") + ".ttl");
        try {
            scenario.model.write(new FileOutputStream(file), "TURTLE");
        } catch (FileNotFoundException e1) {
            return;
        }

        ERMRConnection connection = new ERMRConnection();
        if (file.isFile()) {
            if (Configuration.isValid()) {
                System.out.println("Sending to ERMR.");
                connection.send(file);
            } else {
                System.err.println("Invalid ERMR configuraiton");
            }
        }
    }
}
