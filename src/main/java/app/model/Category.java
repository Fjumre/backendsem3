package app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Table(name = "categories")
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false, unique = true)
    @JsonProperty("CategoryID")
    private int CategoryID;

    @Column(name = "category_name")
    @JsonProperty("CategoryName")
    private String CategoryName;

    public Integer getCategoryId() {
        return this.CategoryID;
    }
}
