package com.rag.infra.persistence.search;

import com.rag.core.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    @Query(value = """
        WITH vector_search AS (
            SELECT id, rank() OVER (ORDER BY embedding <=> cast(:embedding as vector)) AS vector_rank
            FROM documents
            ORDER BY embedding <=> cast(:embedding as vector)
            LIMIT :limit
        ),
        text_search AS (
            SELECT id, rank() OVER (ORDER BY ts_rank_cd(to_tsvector('english', content), plainto_tsquery('english', :query)) DESC) AS text_rank
            FROM documents
            WHERE to_tsvector('english', content) @@ plainto_tsquery('english', :query)
            LIMIT :limit
        ),
        rrf AS (
            SELECT
                COALESCE(v.id, t.id) AS id,
                COALESCE(1.0 / (:rrfK + v.vector_rank), 0.0) + COALESCE(1.0 / (:rrfK + t.text_rank), 0.0) AS rrf_score
            FROM vector_search v
            FULL OUTER JOIN text_search t ON v.id = t.id
        )
        SELECT d.*
        FROM documents d
        JOIN rrf r ON d.id = r.id
        ORDER BY r.rrf_score DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Document> hybridSearch(
            @Param("query") String query,
            @Param("embedding") String embedding,
            @Param("limit") int limit,
            @Param("rrfK") int rrfK
    );
}