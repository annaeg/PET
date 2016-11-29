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

    private Community weatherScientists;
    private ServiceInterface webInterface;
    private TechnicalService dataService;

    private List<HumanAgent> scientists = new ArrayList<HumanAgent>();
    private List<DigitalObject> datasets = new ArrayList<DigitalObject>();

    private DEMRelation lastModified;
    private DEMRelation fileType;
    private DEMRelation fileSize;

    ////////// TEMPLATE //////////// >>>
    /*
     * Method that creates the template for the scenario.
     */
    private void createScenarioTemplate() {
        weatherScientists = new Community(scenario, "Weather Scientists");
        webInterface = new ServiceInterface(scenario, "EUMETSAT Web Portal");
        dataService = new TechnicalService(scenario, "EUMETSAT Data Service");

        webInterface.providesAccessTo(dataService);
        webInterface.isUsedBy(weatherScientists);

        lastModified = new RelationBuilder(scenario, "last modified", CoreModel.digitalObject).create();
        fileType = new RelationBuilder(scenario, "file type", CoreModel.digitalObject).create();
        fileSize = new RelationBuilder(scenario, "file size", CoreModel.digitalObject).create();
        System.out.println("Template generated!");
    }

    /*
     * Creates a new scientist entity.
     */
    private HumanAgent createScientist(String name) {
        HumanAgent scientist = new HumanAgent(scenario, name);
        scientist.isMemberOf(weatherScientists);
        return scientist;
    }

    /*
     * Creates a new dataset entity.
     */
    private DigitalObject createDataset(String name, String last, String type, String size, HumanAgent scientist) {
        DigitalObject dataset = new DigitalObject(scenario, name);
        dataset.addProperty(lastModified, last);
        dataset.addProperty(fileType, type);
        dataset.addProperty(fileSize, size);
        dataset.storedOn(dataService);
        dataset.hasAuthor(scientist);
        return dataset;
    }

    ////////// INFORMATION RECEIVING //////////// >>>
    /**
     * (Skip this method for the demo)
     * This method receives the information extracted by PET.
     */
    public void receivePETInformation(String rawInformation) {
        System.out.println("Information received from PET.");
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try {
            Part information = mapper.readValue(rawInformation, Part.class);
            mapPETData(information);
        } catch (IOException e) {
            System.err.println("[ERROR] Could not map the extracted information.");
        }
    }

    ////////// INFORMATION MAPPING //////////// >>>
    /*
     * This method inserts the extracted information into the template to create Digital Ecosystem Model entities.
     */
    private void mapPETData(Part information){
        System.out.println("Creating entities from templates.");
        // Create a dataset entity:
        DigitalObject dataset = getDataset(information.fileName);

        for (ExtractionResult result : information.extractionResults) {
            if (result.moduleName.equals("Posix file information monitoring")) {
                KeyValueResult kvResult = (KeyValueResult) result.results;

                System.out.println("Mapping information to the entities.");
                // Add information to the dataset:
                dataset.addProperty(lastModified, kvResult.results.get("last_modified"));
                dataset.addProperty(fileSize, kvResult.results.get("file_size"));
                dataset.addProperty(fileType, kvResult.results.get("file_type"));

                // Create a scientist entity:
                HumanAgent scientist = getScientist(kvResult.results.get("file_owner"));

                // Link the scientist to the dataset:
                dataset.addProperty(ProcessModel.hasAuthor, scientist);
            }
        }
        // Send the information to the repository (model store):
        sendModelToERMR();
    }

    /*
     * Returns Digital Object if already existing, or creates new entity otherwise
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

    /*
     * Returns a Human Agent entity if already existing, or creates a new entity otherwise
     */
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

    /*
     * Generate the model, and send it to the ERMR model repository.
     */
    private void sendModelToERMR() {
            File model = generateModel();
            sendModel(model);
    }

    private File generateModel() {
        System.out.println("Generating the Digital Ecosystem Model.");
        String directory = Constants.PROJECT_HOME;
        File file = new File(directory + File.separator + CoreModel.sanitizeName("EcoBuilder_DEMO") + ".ttl");
        try {
            scenario.model.write(new FileOutputStream(file), "TURTLE");
        } catch (FileNotFoundException e) {
            System.err.println("[Error] Couldn't create model.");
        }
        return file;
    }

    private void sendModel(File model) {
        System.out.println("Sending the model to the ERMR model repository!");
        if (model.isFile()) {
            if (Configuration.isValid()) {
                System.out.println("Sending to ERMR.");
                new ERMRConnection().send(model);
            } else {
                System.err.println("Invalid ERMR configuration");
            }
        }
    }
}
