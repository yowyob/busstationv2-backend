package cm.yowyob.bus_station_backend.infrastructure.outbound.persistence.r2dbc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;
import org.springframework.data.r2dbc.dialect.H2Dialect;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
public class R2dbcConfig {

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {
        return R2dbcCustomConversions.of(PostgresDialect.INSTANCE,
                new ListToStringConverter(),
                new StringToListConverter()
        );
    }

    @WritingConverter
    public static class ListToStringConverter implements Converter<List<String>, String> {
        @Override
        public String convert(List<String> source) {
            return String.join(",", source);
        }
    }

    @ReadingConverter
    public static class StringToListConverter implements Converter<String, List<String>> {
        @Override
        public List<String> convert(@NonNull String source) {
            return Arrays.asList(source.split(","));
        }
    }

    @WritingConverter
    public static class UuidListToStringConverter implements Converter<List<UUID>, String> {
        @Override
        public String convert(List<UUID> source) {
            return source.stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining(","));
        }
    }

    @ReadingConverter
    public static class StringToUuidListConverter implements Converter<String, List<UUID>> {
        @Override
        public List<UUID> convert(@NonNull String source) {
            if (source.isEmpty()) return List.of();
            return Arrays.stream(source.split(","))
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
        }
    }
}
