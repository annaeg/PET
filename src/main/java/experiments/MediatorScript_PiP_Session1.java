package experiments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import entities.*;
import model.ExtractionResult;
import model.KeyValueResult;
import model.Part;
import models.CoreModel;
import relations.DEMRelation;
import relations.RelationBuilder;

import java.io.IOException;

/**
 * This will be the Mediator Script of the PERICLES in Practice Session.
 */
public class MediatorScript_PiP_Session1 extends Experiment {

    public MediatorScript_PiP_Session1() {
        super("MediatorScript_PiP_Session1");
        createScenarioTemplate();
    }

    ////////// TEMPLATE //////////// >>>

    private Community artists;
    private ServiceInterface webUpload;
    private TechnicalService artworkStore;
    private DEMRelation lastModified = new RelationBuilder(scenario, "last modified", CoreModel.digitalObject).create();
    private DEMRelation fileType = new RelationBuilder(scenario, "file type", CoreModel.digitalObject).create();
    private DEMRelation fileSize = new RelationBuilder(scenario, "file size", CoreModel.digitalObject).create();

    private void createScenarioTemplate() {
        artists = new Community(scenario, "Artists");
        webUpload = new ServiceInterface(scenario, "Web Upload");
        artworkStore = new TechnicalService(scenario, "Artwork Store");
        webUpload.providesAccessTo(artworkStore);
        webUpload.isUsedBy(artists);
    }

    private HumanAgent createArtist(String name) {
        HumanAgent artist = new HumanAgent(scenario, name);
        artist.isMemberOf(artists);
        return artist;
    }

    //    last_modified :2014-01-24T09:34:12Z
    //    file_type :regularFile
    //    file_size :153224
    //    file_owner :anna
    private DigitalObject createArtwork(String name, String last, String type, String size, HumanAgent artist) {
        DigitalObject artwork = new DigitalObject(scenario, name);
        artwork.addProperty(lastModified, last);
        artwork.addProperty(fileType, type);
        artwork.addProperty(fileSize, size);
        artwork.storedOn(artworkStore);
        artwork.hasAuthor(artist);
        return artwork;
    }

    ////////// INFORMATION GATHERING //////////// >>>

    public void receivePETdata(String collection) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        try {
            Part part = mapper.readValue(collection, Part.class);
            System.out.println(part.fileName);
            for (ExtractionResult result : part.extractionResults) {
                if (result.moduleName.equals("Posix file information monitoring")) {
                    KeyValueResult kvResult = (KeyValueResult) result.results;
                    System.out.println(kvResult.results.get("last_modified"));
                    System.out.println(kvResult.results.get("file_size"));
                    System.out.println(kvResult.results.get("file_type"));
                    System.out.println(kvResult.results.get("file_owner"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    // TODO: What about the events?
    }

    private void sendToERMR() {
        // TODO:
        // 1. generate model
        // 2. send to ERMR
    }
}
