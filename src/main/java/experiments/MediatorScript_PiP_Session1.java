package experiments;

import entities.*;
import models.CoreModel;
import relations.DEMRelation;
import relations.RelationBuilder;

/**
 * This will be the Mediator Script of the PERICLES in Practice Session.
 */
public class MediatorScript_PiP_Session1 extends Experiment {


    public MediatorScript_PiP_Session1() {
        super("MediatorScript_PiP_Session1");
        createScenarioTemplate();
//        receivePETdata();
        sendToERMR();
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

    private HumanAgent createArtist(String name){
        HumanAgent artist = new HumanAgent(scenario, name);
        artist.isMemberOf(artists);
        return artist;
    }

    //    last_modified :2014-01-24T09:34:12Z
    //    file_type :regularFile
    //    file_size :153224
    //    file_owner :anna
    private DigitalObject createArtwork(String name, String last, String type, String size, HumanAgent artist){
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
        System.out.println("CALL");
        System.out.println(collection);
        // TODO: What we want is:
        // "extractionResults", "results" : "last_modified" ,  "file_size" ,  "file_type" , "file_owner"
        // and
        // "fileName"

    }

// TODO: What about the events?

// TODO: This is the format of the collection:

//    {
//        "class" : "model.Part",
//            "extractionResults" : [ {
//        "moduleName" : "Posix file information monitoring",
//                "moduleDisplayName" : "Posix file information monitoring",
//                "moduleVersion" : "0.1",
//                "moduleClass" : "modules.PosixModule",
//                "extractionDate" : "2016-11-22T07:50:33.067+0000",
//                "configurationHash" : "fdded7ff785540a258879766139f2bcf",
//                "results" : {
//            "model.KeyValueResult" : {
//                "results" : {
//                    "last_modified" : "2016-11-18T11:54:53Z",
//                            "last_access" : "2016-11-21T15:31:27Z",
//                            "creation_time" : "2016-11-18T11:54:53Z",
//                            "file_type" : "regularFile",
//                            "file_size" : "79986",
//                            "file_key" : "(dev=803,ino=1847091)",
//                            "file_group_owner" : "anna",
//                            "file_owner" : "anna"
//                },
//                "name" : "PosixResults"
//            }
//        }
//    } ],
//        "fileName" : "eumetsat (copy 1).png",
//            "petVersion" : "0.5",
//            "profileUUID" : "6cd6efd8-d70b-4c1e-8973-3a6066390039"
//    }


    private void sendToERMR() {
        // TODO:
        // 1. generate model
        // 2. send to ERMR
    }
}
