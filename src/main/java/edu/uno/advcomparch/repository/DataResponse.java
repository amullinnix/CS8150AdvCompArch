package edu.uno.advcomparch.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataResponse {

    DataResponseType type;

    // TODO Look at data types or anything else that needs to be encapsulated.
    String data;
}
