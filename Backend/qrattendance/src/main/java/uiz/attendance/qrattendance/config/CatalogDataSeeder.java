package uiz.attendance.qrattendance.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uiz.attendance.qrattendance.model.Major;
import uiz.attendance.qrattendance.model.Module;
import uiz.attendance.qrattendance.repository.MajorRepository;
import uiz.attendance.qrattendance.repository.ModuleRepository;

/**
 * Seeds the (in-memory H2) major/module tables with a small realistic
 * catalog on startup. Runs before {@link StudentDataSeeder} (see its
 * {@code @Order}) so the majors it assigns to students already exist.
 */
@Component
@Order(1)
public class CatalogDataSeeder implements CommandLineRunner {

    private final MajorRepository majorRepository;
    private final ModuleRepository moduleRepository;

    public CatalogDataSeeder(MajorRepository majorRepository, ModuleRepository moduleRepository) {
        this.majorRepository = majorRepository;
        this.moduleRepository = moduleRepository;
    }

    @Override
    public void run(String... args) {
        Major gi = seedMajor("GI", "Génie Informatique");
        Major smi = seedMajor("SMI", "Sciences Mathématiques et Informatique");
        Major gc = seedMajor("GC", "Génie Civil");

        seedModule("M131", "Algorithmique et Structures de Données", 3, gi);
        seedModule("M132", "Bases de Données", 3, gi);
        seedModule("M201", "Analyse Numérique", 2, smi);
        seedModule("M202", "Probabilités et Statistiques", 2, smi);
        seedModule("M301", "Résistance des Matériaux", 3, gc);
    }

    private Major seedMajor(String code, String name) {
        if (majorRepository.existsByCode(code)) {
            return majorRepository.findByCode(code).orElseThrow();
        }
        Major major = new Major();
        major.setCode(code);
        major.setName(name);
        return majorRepository.save(major);
    }

    private void seedModule(String code, String name, int semester, Major major) {
        if (moduleRepository.existsByCode(code)) {
            return;
        }
        Module module = new Module();
        module.setCode(code);
        module.setName(name);
        module.setSemester(semester);
        module.setMajor(major);
        moduleRepository.save(module);
    }
}
