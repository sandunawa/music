package com.melodymart.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.melodymart.model.*;
import jakarta.annotation.PostConstruct;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class FileStorageService {

    @Value("${melodymart.data-dir}")
    private String dataDir;

    private final ObjectMapper objectMapper;
    private final Map<String, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();

    public FileStorageService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @PostConstruct
    public void init() {
        try {
            // Create data directory if it doesn't exist
            Files.createDirectories(Paths.get(dataDir));

            // Initialize files and seed them if empty
            initFile("users.json", this::getSeedUsers);
            initFile("products.json", this::getSeedProducts);
            initFile("orders.json", ArrayList::new);
            initFile("rentals.json", ArrayList::new);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize file storage directory: " + dataDir, e);
        }
    }

    private ReentrantReadWriteLock getLock(String filename) {
        return locks.computeIfAbsent(filename, k -> new ReentrantReadWriteLock());
    }

    private <T> void initFile(String filename, java.util.function.Supplier<List<T>> seedSupplier) throws IOException {
        File file = new File(dataDir, filename);
        if (!file.exists() || file.length() == 0) {
            List<T> seedData = seedSupplier.get();
            writeList(filename, seedData);
        }
    }

    /**
     * Reads a list of items of a specified class from a JSON file in a thread-safe manner.
     */
    public <T> List<T> readList(String filename, Class<T> valueType) {
        ReentrantReadWriteLock.ReadLock readLock = getLock(filename).readLock();
        readLock.lock();
        try {
            File file = new File(dataDir, filename);
            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, valueType));
        } catch (IOException e) {
            throw new RuntimeException("Error reading data from " + filename, e);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Writes a list of items to a JSON file in a thread-safe manner.
     */
    public <T> void writeList(String filename, List<T> list) {
        ReentrantReadWriteLock.WriteLock writeLock = getLock(filename).writeLock();
        writeLock.lock();
        try {
            File file = new File(dataDir, filename);
            objectMapper.writeValue(file, list);
        } catch (IOException e) {
            throw new RuntimeException("Error writing data to " + filename, e);
        } finally {
            writeLock.unlock();
        }
    }

    // Seed Data Generators
    private List<User> getSeedUsers() {
        List<User> users = new ArrayList<>();
        // Default Admin User (Password: admin123)
        users.add(new User(
                "u-admin",
                "Alex Mercer (Admin)",
                "admin@melodymart.com",
                BCrypt.hashpw("admin123", BCrypt.gensalt()),
                Role.ADMIN,
                "+1 (555) 019-2831",
                "MelodyMart Corporate HQ, Boston, MA"
        ));
        // Default Customer User (Password: customer123)
        users.add(new User(
                "u-customer",
                "Sophia Reynolds",
                "customer@melodymart.com",
                BCrypt.hashpw("customer123", BCrypt.gensalt()),
                Role.CUSTOMER,
                "+1 (555) 014-9988",
                "742 Evergreen Terrace, Springfield, OR"
        ));
        return users;
    }

    private List<Product> getSeedProducts() {
        List<Product> products = new ArrayList<>();

        // Guitars
        Map<String, String> specs1 = new LinkedHashMap<>();
        specs1.put("Body", "Alder");
        specs1.put("Neck", "Maple (Modern C Profile)");
        specs1.put("Fingerboard", "Maple, 9.5\" Radius");
        specs1.put("Pickups", "3x Player Series Alnico 5 Strat Single-Coil");
        products.add(new Product("p-git-1", "Fender Player Stratocaster Electric Guitar", Category.GUITAR,
                "Fender", "The inspiring sound of a Stratocaster is one of the foundations of Fender. Authentic Fender tone and style with modern player comforts.",
                849.99, 25.00, 5,
                "https://images.unsplash.com/photo-1550985616-10810253b84d?q=80&w=600&auto=format&fit=crop", specs1));

        Map<String, String> specs2 = new LinkedHashMap<>();
        specs2.put("Top", "Solid Sitka Spruce");
        specs2.put("Back & Sides", "Mahogany");
        specs2.put("Neck", "Mahogany (SlimTaper D)");
        specs2.put("Electronics", "Fishman Presys II preamp");
        products.add(new Product("p-git-2", "Epiphone EJ-200SCE Acoustic-Electric Guitar", Category.GUITAR,
                "Epiphone", "The King of the Flat-tops, updated with a cutaway and Fishman electronics for outstanding live acoustic projection.",
                549.00, 18.00, 8,
                "https://images.unsplash.com/photo-1510915361894-db8b60106cb1?q=80&w=600&auto=format&fit=crop", specs2));

        // Keyboards
        Map<String, String> specs3 = new LinkedHashMap<>();
        specs3.put("Keys", "88 Weighted (Graded Hammer Standard)");
        specs3.put("Polyphony", "192 Notes");
        specs3.put("Voices", "24 Instrument Voices");
        specs3.put("Outputs", "2x Headphone Jack, USB to Host");
        products.add(new Product("p-key-1", "Yamaha P-125 88-Key Digital Piano", Category.KEYBOARD,
                "Yamaha", "A compact digital piano that combines incredible piano performance with a user-friendly, minimalist design.",
                699.99, 22.00, 4,
                "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?q=80&w=600&auto=format&fit=crop", specs3));

        // Drums
        Map<String, String> specs4 = new LinkedHashMap<>();
        specs4.put("Drum Sound Module", "TD-17");
        specs4.put("Snare Pad", "PDX-12 (12-inch double-mesh)");
        specs4.put("Tom Pads", "3x PDX-8 (8-inch double-mesh)");
        specs4.put("Cymbal Pads", "CY-5 Hi-Hat, CY-12C Crash, CY-13R Ride");
        products.add(new Product("p-drum-1", "Roland TD-17KVX Electronic Drum Kit", Category.DRUMS,
                "Roland", "The ultimate electronic drumming experience. Features natural-feeling mesh pads, custom kit uploads, and advanced learning tools.",
                1499.00, 45.00, 3,
                "https://images.unsplash.com/photo-1524486361537-8ad15938e1a3?q=80&w=600&auto=format&fit=crop", specs4));

        // Studio & Audio Gear
        Map<String, String> specs5 = new LinkedHashMap<>();
        specs5.put("Type", "Dynamic");
        specs5.put("Polar Pattern", "Cardioid");
        specs5.put("Frequency Range", "50Hz - 20kHz");
        specs5.put("Connection", "XLR");
        products.add(new Product("p-studio-1", "Shure SM7B Vocal Microphone", Category.STUDIO_AUDIO,
                "Shure", "The industry-standard vocal mic for broadcasting, podcasting, and studio tracking. Legendary warm and smooth sound.",
                399.00, 15.00, 10,
                "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?q=80&w=600&auto=format&fit=crop", specs5));

        // Brass & Woodwinds
        Map<String, String> specs6 = new LinkedHashMap<>();
        specs6.put("Key", "Eb (Alto)");
        specs6.put("Body Material", "Yellow Brass (Gold Lacquer)");
        specs6.put("Auxiliary Keys", "High F#, Front F");
        specs6.put("Case", "Included hard shell gig bag");
        products.add(new Product("p-brass-1", "Yamaha YAS-280 Student Alto Saxophone", Category.BRASS_WOODWIND,
                "Yamaha", "Provides beginners with a reliable instrument to start learning. Offers a bright saxophone sound with excellent intonation.",
                1149.00, 35.00, 3,
                "https://images.unsplash.com/photo-1528143358888-6d3c7f67bd5d?q=80&w=600&auto=format&fit=crop", specs6));

        // Accessories
        Map<String, String> specs7 = new LinkedHashMap<>();
        specs7.put("Driver Size", "45 mm Neodymium");
        specs7.put("Frequency Response", "15Hz - 28kHz");
        specs7.put("Impedance", "38 Ohms");
        specs7.put("Cables", "3 detachable cables (coiled, straight short, straight long)");
        products.add(new Product("p-acc-1", "Audio-Technica ATH-M50x Monitor Headphones", Category.ACCESSORIES,
                "Audio-Technica", "Critically acclaimed studio monitoring headphones offering exceptional clarity, deep bass response, and comfortable sound isolation.",
                149.00, 5.00, 20,
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?q=80&w=600&auto=format&fit=crop", specs7));

        return products;
    }
}
