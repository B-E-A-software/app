package ro.unibuc.hello.data;

import lombok.*;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "requests")
@Data
public class RequestEntity {

    @Id
    private String id; 

    private String username;
    private String toDoList;

    private String description; 

    public RequestEntity(String user, String toDoList, String description) {
        this.id = user + toDoList;
        this.username = user;
        this.toDoList = toDoList;
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format(
                "RequestEntity[user='%s', toDoList='%s', description='%s']",
                username, toDoList, description);
    }
}
