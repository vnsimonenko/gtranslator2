package gtranslator.repositary;

import gtranslator.domain.Dictionary;
import gtranslator.domain.Language;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface DictionaryRepositary extends CrudRepository<Dictionary, Long> {
    //@Cacheable(cacheNames = "dictionary", key = "{#p0, #p1, #p2, #p3}")
    //@EntityGraph(value = "Dictionary.source", type = EntityGraph.EntityGraphType.LOAD)
    @Query(value = "from Dictionary d where d.source.text = ?1 and d.source.lang = ?2 and d.target.text = ?3 and d.target.lang = ?4")
    Dictionary find(String text, Language srcLang, String target, Language trgLang);

    //@Cacheable(cacheNames = "dictionary2", key = "{#p0, #p1, #p2}")
    @Query(value = "from Dictionary d where d.source.text = ?1 and d.source.lang = ?2 and d.target.lang = ?3")
    List<Dictionary> find(String text, Language srcLang, Language trgLang);

    @Caching(evict = {
            @CacheEvict(cacheNames = "dictionary", key = "{#p0.source.text, #p0.source.lang, #p0.target.text, #p0.target.lang}"),
            @CacheEvict(cacheNames = "dictionary2", key = "{#p0.source.text, #p0.source.lang, #p0.target.lang}")})
    Dictionary save(Dictionary dictionary);
}
