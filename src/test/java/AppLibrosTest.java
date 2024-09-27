import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ar.edu.dds.libros.Libro;
import ar.edu.dds.libros.RepoLibros;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Collection;

public class AppLibrosTest {



    @Test
    public void testAlgoQueDeberiaEstarBien() {
        Assertions.assertEquals(1, 1);
    }

     @Test
    public void testAgregarLibro() {
        // Configura el EntityManagerFactory para las pruebas (puedes usar un H2 in-memory para esto)
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test-pu");
        EntityManager em = emf.createEntityManager();
        
        // Crea un libro
        Libro libro = new Libro();
        libro.setNombre("El Principito");
        libro.setAutor("Antoine de Saint-Exupéry");
        libro.setPrecio(150L);

        // Guarda el libro
        RepoLibros repo = new RepoLibros(em);
        em.getTransaction().begin();
        repo.save(libro);
        em.getTransaction().commit();

        // Verifica que el libro se haya guardado correctamente
        Collection<Libro> libros = repo.findAll();
        Assertions.assertEquals(1, libros.size());
        Assertions.assertEquals("El Principito", libros.iterator().next().getNombre());
        
        em.close();
        emf.close();
    }

  

}
